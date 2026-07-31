package net.cocotea.cyreneai.service.rag.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.dto.AiSttModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiSttModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiSttModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.SttTranscribeDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.util.ApiKeyCipher;
import net.cocotea.cyreneai.util.SafeHttpUtils;
import net.cocotea.cyreneai.model.po.AiSttRecord;
import net.cocotea.cyreneai.model.vo.AiSttModelVO;
import net.cocotea.cyreneai.model.vo.AiSttRecordVO;
import net.cocotea.cyreneai.service.rag.SttService;
import net.cocotea.cyreneadmin.model.ApiPage;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SttServiceImpl implements SttService {

    private static final String DEFAULT_WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    @Db
    private LightDao lightDao;

    @Override
    public String transcribe(SttTranscribeDTO dto) {
        AiModel model = getModelById(dto.getModelId());
        if (model == null) {
            model = getDefaultModel();
        }
        if (model == null) {
            throw new RuntimeException("未找到可用的语音转文字模型配置");
        }

        byte[] audioBytes;
        try {
            // SSRF 防护 + 大小限制下载
            audioBytes = SafeHttpUtils.downloadBytes(dto.getAudioUrl());
        } catch (Exception e) {
            throw new RuntimeException("下载音频文件失败: " + e.getMessage());
        }

        String fileName = dto.getAudioUrl().substring(dto.getAudioUrl().lastIndexOf('/') + 1);
        if (fileName.isBlank()) {
            fileName = "audio.mp3";
        }

        return transcribeBytes(model, audioBytes, fileName);
    }

    @Override
    public String transcribeFile(InputStream fileStream, String fileName, BigInteger modelId) {
        AiModel model;
        if (modelId != null) {
            model = getModelById(modelId);
        } else {
            model = getDefaultModel();
        }
        if (model == null) {
            throw new RuntimeException("未找到可用的语音转文字模型配置");
        }

        byte[] audioBytes;
        try {
            audioBytes = fileStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取音频文件失败: " + e.getMessage());
        }

        return transcribeBytes(model, audioBytes, fileName);
    }

    private String transcribeBytes(AiModel model, byte[] audioBytes, String fileName) {
        AiModelProvider provider = getProvider(model.getProviderId());
        String apiKey = resolveApiKey(provider);

        if (provider != null && "dashscope".equals(provider.getProviderType())) {
            return transcribeWithDashScope(provider, model, audioBytes, fileName);
        }

        String baseUrl = provider != null ? provider.getApiBaseUrl() : null;
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_WHISPER_URL;
        }
        String apiUrl;
        if (baseUrl.contains("/v1/audio/transcriptions")) {
            apiUrl = baseUrl;
        } else if (baseUrl.endsWith("/v1") || baseUrl.endsWith("/v1/")) {
            apiUrl = baseUrl.replaceAll("/+$", "") + "/audio/transcriptions";
        } else {
            apiUrl = baseUrl.replaceAll("/+$", "") + "/v1/audio/transcriptions";
        }

        log.info("STT OpenAI request URL: {}, provider: {}, model: {}, fileSize: {}, fileName: {}",
                apiUrl, provider != null ? provider.getProviderType() : null,
                model.getModelName(), audioBytes.length, fileName);

        try {
            HttpRequest request = HttpUtil.createPost(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .form("file", audioBytes, fileName)
                    .form("model", model.getModelName() != null ? model.getModelName() : "whisper-1")
                    .form("response_format", "json")
                    .timeout(120000);

            String responseBody = request.execute().body();
            JSONObject json = JSONUtil.parseObj(responseBody);

            if (json.containsKey("error")) {
                throw new RuntimeException("Whisper API 错误: " + json.getJSONObject("error").getStr("message"));
            }

            String transcript = json.getStr("text");

            saveRecord(model, audioBytes.length, transcript);

            return transcript;
        } catch (Exception e) {
            log.error("语音转文字失败, apiUrl: {}, provider: {}", apiUrl,
                    provider != null ? provider.getProviderType() : null, e);
            throw new RuntimeException("语音转文字失败: " + e.getMessage());
        }
    }

    private String transcribeWithDashScope(AiModelProvider provider, AiModel model, byte[] audioBytes, String fileName) {
        String apiKey = resolveApiKey(provider);
        String rawBase = provider.getApiBaseUrl();
        if (rawBase == null || rawBase.isBlank()) {
            rawBase = "https://dashscope.aliyuncs.com";
        }
        rawBase = rawBase.replaceAll("/+$", "");
        String dashScopeBase = rawBase.endsWith("/api/v1") ? rawBase : rawBase + "/api/v1";

        String apiPath = "/api/v1/services/aigc/multimodal-generation/generation";
        String url = dashScopeBase.endsWith("/api/v1") ? dashScopeBase + apiPath.replace("/api/v1", "") : dashScopeBase + apiPath;

        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "wav";
        String mimeType = switch (ext) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg", "opus" -> "audio/opus";
            case "m4a", "aac" -> "audio/mp4";
            case "flac" -> "audio/flac";
            default -> "audio/wav";
        };
        String base64 = Base64.getEncoder().encodeToString(audioBytes);
        String dataUri = "data:" + mimeType + ";base64," + base64;

        String modelName = model.getModelName() != null ? model.getModelName() : "fun-asr-flash-2026-06-15";

        JSONObject inputAudio = new JSONObject();
        inputAudio.set("data", dataUri);

        JSONObject contentItem = new JSONObject();
        contentItem.set("type", "input_audio");
        contentItem.set("input_audio", inputAudio);

        JSONArray content = new JSONArray();
        content.add(contentItem);

        JSONObject msg = new JSONObject();
        msg.set("role", "user");
        msg.set("content", content);

        JSONArray messages = new JSONArray();
        messages.add(msg);

        JSONObject input = new JSONObject();
        input.set("messages", messages);

        JSONObject parameters = new JSONObject();
        parameters.set("format", ext);

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", modelName);
        requestBody.set("input", input);
        requestBody.set("parameters", parameters);

        log.info("DashScope STT request URL: {}, model: {}, fileSize: {}, format: {}",
                url, modelName, audioBytes.length, ext);

        try {
            String responseBody = HttpUtil.createPost(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(120000)
                    .execute()
                    .body();

            log.info("DashScope STT response: {}", responseBody);

            JSONObject json = JSONUtil.parseObj(responseBody);
            if (json.containsKey("code") || json.containsKey("error")) {
                String errMsg = json.containsKey("error")
                        ? json.getJSONObject("error").getStr("message")
                        : json.getStr("message");
                throw new RuntimeException("DashScope 语音转文字失败: " + errMsg);
            }

            String transcript = json.getByPath("output.text", String.class);
            if (transcript == null || transcript.isBlank()) {
                throw new RuntimeException("DashScope 语音转文字返回结果为空");
            }

            saveRecord(model, audioBytes.length, transcript);

            return transcript;
        } catch (Exception e) {
            log.error("DashScope 语音转文字失败", e);
            throw new RuntimeException("DashScope 语音转文字失败: " + e.getMessage());
        }
    }

    private void saveRecord(AiModel model, long fileSize, String transcript) {
        AiSttRecord record = new AiSttRecord();
        record.setAudioUrl(null);
        record.setModelName(model.getModelName());
        record.setTranscript(transcript);
        record.setFileSize(fileSize);
        record.setDurationSeconds(null);
        record.setCost(BigDecimal.ZERO);
        lightDao.save(record);
    }

    private AiModelProvider getProvider(BigInteger providerId) {
        if (providerId == null) return null;
        return lightDao.load(new AiModelProvider(providerId));
    }

    private AiModelProvider getProviderByType(String providerType) {
        List<AiModelProvider> providers = lightDao.findEntity(
                AiModelProvider.class,
                EntityQuery.create()
                        .where("provider_type = ? and enable_status = 1 and is_deleted = 0")
                        .values(providerType)
                        .orderByDesc("sort"));
        return providers.isEmpty() ? null : providers.getFirst();
    }

    private AiModel getModelById(BigInteger id) {
        if (id == null) return null;
        return lightDao.load(new AiModel(id));
    }

    private AiModel getDefaultModel() {
        EntityQuery query = EntityQuery.create()
                .where("model_type = 'stt' and is_default = 1 and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        List<AiModel> models = lightDao.findEntity(AiModel.class, query);
        if (models.isEmpty()) {
            query = EntityQuery.create()
                    .where("model_type = 'stt' and enable_status = 1 and is_deleted = 0")
                    .orderByDesc("sort");
            models = lightDao.findEntity(AiModel.class, query);
        }
        return models.isEmpty() ? null : models.getFirst();
    }

    private String resolveApiKey(AiModelProvider provider) {
        if (provider != null && provider.getApiKey() != null && !provider.getApiKey().isBlank()) {
            return ApiKeyCipher.decrypt(provider.getApiKey());
        }
        String envKey = System.getenv("OPENAI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey;
        }
        throw new RuntimeException("未配置API密钥");
    }

    @Override
    public List<AiSttModelVO> listModels() {
        EntityQuery eq = EntityQuery.create()
                .where("model_type = 'stt' and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        return lightDao.findEntity(AiModel.class, eq).stream()
                .map(m -> {
                    AiModelProvider provider = getProvider(m.getProviderId());
                    AiSttModelVO vo = new AiSttModelVO();
                    vo.setId(m.getId());
                    vo.setProviderType(provider != null ? provider.getProviderType() : null);
                    vo.setModelName(m.getModelName());
                    vo.setApiBaseUrl(provider != null ? provider.getApiBaseUrl() : null);
                    vo.setIsDefault(m.getIsDefault());
                    vo.setEnableStatus(m.getEnableStatus());
                    vo.setSort(m.getSort());
                    vo.setRemark(m.getRemark());
                    return vo;
                }).toList();
    }

    @Override
    public boolean add(AiSttModelAddDTO dto) {
        AiModel model = new AiModel();
        model.setModelType("stt");
        if (dto.getProviderType() != null) {
            AiModelProvider provider = getProviderByType(dto.getProviderType());
            if (provider != null) {
                model.setProviderId(provider.getId());
            }
        }
        model.setModelName(dto.getModelName());
        model.setIsDefault(dto.getIsDefault());
        model.setEnableStatus(dto.getEnableStatus());
        model.setSort(dto.getSort());
        model.setRemark(dto.getRemark());
        return lightDao.save(model) != null;
    }

    @Override
    public boolean update(AiSttModelUpdateDTO dto) {
        AiModel model = new AiModel();
        model.setId(dto.getId());
        model.setModelType("stt");
        if (dto.getProviderType() != null) {
            AiModelProvider provider = getProviderByType(dto.getProviderType());
            if (provider != null) {
                model.setProviderId(provider.getId());
            }
        }
        model.setModelName(dto.getModelName());
        model.setIsDefault(dto.getIsDefault());
        model.setEnableStatus(dto.getEnableStatus());
        model.setSort(dto.getSort());
        model.setRemark(dto.getRemark());
        Long updated = lightDao.update(model);
        return updated != null && updated > 0;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        for (BigInteger id : idList) {
            AiModel model = new AiModel(id);
            model.setIsDeleted(1);
            lightDao.update(model);
        }
        return !idList.isEmpty();
    }

    @Override
    public ApiPage<AiSttModelVO> listByPage(AiSttModelPageDTO pageDTO) {
        AiSttModelPageDTO.Query q = pageDTO.getSttModel();
        Map<String, Object> params = MapUtil.newHashMap(3);
        params.put("providerType", q != null ? q.getProviderType() : null);
        params.put("modelName", q != null ? q.getModelName() : null);
        params.put("enableStatus", q != null ? q.getEnableStatus() : null);
        Page<AiSttModelVO> pageParam = new Page<>();
        pageParam.setPageNo(pageDTO.getPageNo());
        pageParam.setPageSize(pageDTO.getPageSize());
        Page<AiSttModelVO> page = lightDao.findPage(pageParam, "ai_stt_model_findList", params, AiSttModelVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public List<AiSttModelVO> listEnabled() {
        EntityQuery eq = EntityQuery.create()
                .where("model_type = 'stt' and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        return lightDao.findEntity(AiModel.class, eq).stream()
                .map(m -> {
                    AiModelProvider provider = getProvider(m.getProviderId());
                    AiSttModelVO vo = new AiSttModelVO();
                    vo.setId(m.getId());
                    vo.setProviderType(provider != null ? provider.getProviderType() : null);
                    vo.setModelName(m.getModelName());
                    vo.setApiBaseUrl(provider != null ? provider.getApiBaseUrl() : null);
                    vo.setIsDefault(m.getIsDefault());
                    vo.setEnableStatus(m.getEnableStatus());
                    vo.setSort(m.getSort());
                    vo.setRemark(m.getRemark());
                    return vo;
                }).toList();
    }

    @Override
    public ApiPage<AiSttRecordVO> listHistoryByPage(AiSttModelPageDTO pageDTO) {
        EntityQuery eq = EntityQuery.create().orderByDesc("create_time");
        Page<AiSttRecord> page = lightDao.findPageEntity(
                ApiPage.create(pageDTO),
                AiSttRecord.class,
                eq
        );
        List<AiSttRecordVO> rows = page.getRows().stream().map(r -> {
            AiSttRecordVO vo = new AiSttRecordVO();
            vo.setId(r.getId());
            vo.setCreateBy(r.getCreateBy());
            vo.setAudioUrl(r.getAudioUrl());
            vo.setModelName(r.getModelName());
            vo.setTranscript(r.getTranscript());
            vo.setFileSize(r.getFileSize());
            vo.setDurationSeconds(r.getDurationSeconds());
            vo.setCost(r.getCost());
            vo.setCreateTime(r.getCreateTime());
            vo.setUpdateBy(r.getUpdateBy());
            vo.setUpdateTime(r.getUpdateTime());
            return vo;
        }).toList();
        return ApiPage.rest(page, rows);
    }
}
