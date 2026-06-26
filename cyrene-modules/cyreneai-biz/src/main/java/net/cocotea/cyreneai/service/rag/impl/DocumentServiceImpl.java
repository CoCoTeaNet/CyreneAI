package net.cocotea.cyreneai.service.rag.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import dev.langchain4j.data.embedding.Embedding;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.po.AiDocument;
import net.cocotea.cyreneai.model.vo.AiDocumentVO;
import net.cocotea.cyreneai.model.po.AiDocumentChunk;
import net.cocotea.cyreneai.model.po.AiEmbeddingModel;
import net.cocotea.cyreneai.model.po.AiKbDocument;
import net.cocotea.cyreneai.service.rag.DocumentService;
import net.cocotea.cyreneai.service.rag.EmbeddingService;
import net.cocotea.cyreneai.service.rag.TextSplitter;
import net.cocotea.cyreneai.service.rag.VectorStore;
import net.cocotea.cyreneadmin.model.ApiPage;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DocumentServiceImpl implements DocumentService {

    @Db
    private LightDao lightDao;

    @Inject
    private EmbeddingService embeddingService;

    @Inject
    private VectorStore vectorStore;

    private final Tika tika = new Tika();

    private static final String DOC_STORAGE_DIR = System.getProperty("user.dir") + "/data/documents/";

    @Override
    public AiDocument upload(String fileName, byte[] fileContent, BigInteger kbId,
                             String chunkStrategy, Integer chunkSize, Integer chunkOverlap) {
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "txt";
        BigInteger userId = BigInteger.valueOf(StpUtil.getLoginIdAsLong());

        // Save file to disk
        try {
            java.io.File dir = new java.io.File(DOC_STORAGE_DIR);
            if (!dir.exists()) dir.mkdirs();
            String storedFileName = System.currentTimeMillis() + "_" + fileName;
            java.io.File target = new java.io.File(dir, storedFileName);
            java.nio.file.Files.write(target.toPath(), fileContent);
        } catch (IOException e) {
            log.error("Failed to save file: {}", fileName, e);
        }

        AiDocument doc = new AiDocument();
        doc.setName(fileName);
        doc.setType(ext);
        doc.setSize((long) fileContent.length);
        doc.setFilePath(DOC_STORAGE_DIR + System.currentTimeMillis() + "_" + fileName);
        doc.setStatus(0);
        doc.setChunkStrategy(chunkStrategy != null ? chunkStrategy : "paragraph");
        doc.setChunkSize(chunkSize != null ? chunkSize : 500);
        doc.setChunkOverlap(chunkOverlap != null ? chunkOverlap : 50);
        doc.setKbId(kbId);
        doc.setCreateBy(userId);
        doc.setCreateTime(LocalDateTime.now());
        lightDao.save(doc);

        // Process asynchronously in a separate thread
        byte[] contentCopy = fileContent.clone();
        BigInteger docId = doc.getId();
        new Thread(() -> {
            try {
                processDocumentContent(docId, fileName, contentCopy, kbId,
                        doc.getChunkStrategy(), doc.getChunkSize(), doc.getChunkOverlap());
            } catch (Exception e) {
                log.error("Failed to process document: {}", fileName, e);
                AiDocument d = lightDao.load(new AiDocument(docId));
                if (d != null) {
                    d.setStatus(3);
                    d.setErrorMsg(e.getMessage());
                    lightDao.update(d);
                }
            }
        }).start();

        return doc;
    }

    private void processDocumentContent(BigInteger docId, String fileName, byte[] content,
                                        BigInteger kbId, String chunkStrategy,
                                        Integer chunkSize, Integer chunkOverlap) {
        AiDocument doc = lightDao.load(new AiDocument(docId));
        if (doc == null) return;

        doc.setStatus(1);
        lightDao.update(doc);

        String text = extractText(fileName, content);
        if (text == null || text.isBlank()) {
            doc.setStatus(3);
            doc.setErrorMsg("No extractable text found");
            lightDao.update(doc);
            return;
        }

        List<String> chunks = TextSplitter.split(text, chunkStrategy, chunkSize, chunkOverlap);
        log.info("Document {} split into {} chunks", fileName, chunks.size());

        AiEmbeddingModel embeddingModel = embeddingService.getDefaultEmbeddingModel();
        if (embeddingModel == null) {
            doc.setStatus(3);
            doc.setErrorMsg("No embedding model configured");
            lightDao.update(doc);
            return;
        }

        List<Embedding> embeddings = embeddingService.embedBatch(chunks, embeddingModel);
        List<AiDocumentChunk> chunkEntities = new ArrayList<>();
        List<float[]> embeddingVectors = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            AiDocumentChunk chunkEntity = new AiDocumentChunk();
            chunkEntity.setDocumentId(docId);
            chunkEntity.setKbId(kbId);
            chunkEntity.setContent(chunks.get(i));
            chunkEntity.setChunkIndex(i);
            chunkEntity.setCreateTime(LocalDateTime.now());
            chunkEntities.add(chunkEntity);

            if (i < embeddings.size()) {
                embeddingVectors.add(embeddings.get(i).vector());
            } else {
                embeddingVectors.add(null);
            }
        }

        vectorStore.addChunks(chunkEntities, embeddingVectors);

        if (kbId != null) {
            AiKbDocument kbDoc = new AiKbDocument();
            kbDoc.setKbId(kbId);
            kbDoc.setDocumentId(docId);
            kbDoc.setCreateTime(LocalDateTime.now());
            try {
                lightDao.save(kbDoc);
            } catch (Exception e) {
                log.warn("kb_document relation may already exist: {}", e.getMessage());
            }
        }

        doc.setChunkCount(chunks.size());
        doc.setStatus(2);
        lightDao.update(doc);
        log.info("Document {} processed successfully with {} chunks", fileName, chunks.size());
    }

    private String extractText(String fileName, byte[] content) {
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "";
        return switch (ext) {
            case "txt", "md" -> new String(content, StandardCharsets.UTF_8);
            case "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls" -> {
                try {
                    yield tika.parseToString(new ByteArrayInputStream(content));
                } catch (TikaException | IOException e) {
                    log.error("Tika parse failed for {}: {}", fileName, e.getMessage());
                    yield null;
                }
            }
            default -> new String(content, StandardCharsets.UTF_8);
        };
    }

    @Override
    public void reIndex(BigInteger documentId) {
        vectorStore.removeByDocumentId(documentId);
        AiDocument doc = lightDao.load(new AiDocument(documentId));
        if (doc == null) return;

        doc.setStatus(0);
        doc.setChunkCount(0);
        doc.setErrorMsg(null);
        lightDao.update(doc);

        new Thread(() -> {
            try {
                byte[] content = readFileContent(doc);
                if (content == null) {
                    doc.setStatus(3);
                    doc.setErrorMsg("File not found for re-indexing");
                    lightDao.update(doc);
                    return;
                }
                processDocumentContent(documentId, doc.getName(), content,
                        doc.getKbId(), doc.getChunkStrategy(), doc.getChunkSize(), doc.getChunkOverlap());
            } catch (Exception e) {
                log.error("Re-index failed for document: {}", doc.getName(), e);
                doc.setStatus(3);
                doc.setErrorMsg(e.getMessage());
                lightDao.update(doc);
            }
        }).start();
    }

    private byte[] readFileContent(AiDocument doc) {
        if (doc.getFilePath() != null) {
            java.io.File file = new java.io.File(doc.getFilePath());
            if (file.exists()) {
                try {
                    return java.nio.file.Files.readAllBytes(file.toPath());
                } catch (IOException e) {
                    log.error("Failed to read file: {}", doc.getFilePath(), e);
                }
            }
        }
        return null;
    }

    @Override
    public void delete(BigInteger id) {
        vectorStore.removeByDocumentId(id);
        AiDocument doc = new AiDocument(id);
        doc.setIsDeleted(1);
        lightDao.update(doc);
    }

    @Override
    public AiDocument getById(BigInteger id) {
        return lightDao.load(new AiDocument(id));
    }

    @Override
    public ApiPage<AiDocumentVO> listByPage(AiDocument query, int pageNo, int pageSize) {
        Map<String, Object> params = MapUtil.newHashMap(4);
        params.put("name", query.getName());
        params.put("type", query.getType());
        params.put("status", query.getStatus());
        params.put("kbId", query.getKbId());
        Page<AiDocumentVO> pageParam = new Page<>();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        Page<AiDocumentVO> page = lightDao.findPage(
                pageParam,
                "ai_document_findList",
                params,
                AiDocumentVO.class
        );
        return ApiPage.rest(page);
    }

    @Override
    public void processDocument(AiDocument document) {
        String filePath = document.getFilePath();
        if (filePath == null) return;
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            document.setStatus(3);
            document.setErrorMsg("File not found");
            lightDao.update(document);
            return;
        }
        try {
            byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
            processDocumentContent(document.getId(), document.getName(), content,
                    document.getKbId(), document.getChunkStrategy(),
                    document.getChunkSize(), document.getChunkOverlap());
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            document.setStatus(3);
            document.setErrorMsg(e.getMessage());
            lightDao.update(document);
        }
    }
}
