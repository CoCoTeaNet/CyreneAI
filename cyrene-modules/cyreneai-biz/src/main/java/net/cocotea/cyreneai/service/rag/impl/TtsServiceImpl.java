package net.cocotea.cyreneai.service.rag.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiPageDTO;
import net.cocotea.cyreneai.model.dto.AiTtsModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiTtsModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiTtsModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.TtsSynthesizeDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.model.po.AiTtsRecord;
import net.cocotea.cyreneai.model.vo.AiTtsModelVO;
import net.cocotea.cyreneai.model.vo.AiTtsRecordVO;
import net.cocotea.cyreneai.service.rag.TtsService;
import net.cocotea.cyreneai.util.ApiKeyCipher;
import net.cocotea.cyreneai.util.TextSegmenter;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TtsServiceImpl implements TtsService {

    private static final String DEFAULT_TTS_URL = "https://api.openai.com/v1/audio/speech";
    private static final String AUDIO_DIR = "uploads/audio/tts/";

    /** OpenAI TTS 单次输入字符上限（官方约 4096，预留余量）。 */
    private static final int OPENAI_TTS_MAX_CHARS = 4000;

    private static final Map<String, List<String>> PROVIDER_VOICES = Map.of(
            "openai", List.of("alloy", "echo", "fable", "nova", "onyx", "shimmer"),
            "dashscope", List.of("longanyang", "longxiaochun", "longhua", "longting", "longyi",
                    "aiqian", "aixiang", "aijun", "aibao", "aijia", "ainai", "aimei", "aijing",
                    "aifan", "aiqi", "aishuang", "aiyi", "aiyue", "aizhe", "aixuan", "aifang",
                    "aiguo", "aian", "aijing_plus", "aiyu", "aiyue_plus", "longshu", "yunjian",
                    "longding", "longxing", "longwan", "longyu", "longping", "longzuo")
    );

    @Db
    private LightDao lightDao;

    @Override
    public byte[] synthesize(TtsSynthesizeDTO dto) {
        AiModel model;
        if (dto.getModelId() != null) {
            model = lightDao.load(new AiModel(dto.getModelId()));
        } else {
            model = getDefaultModel();
        }
        if (model == null) {
            throw new RuntimeException("未找到可用的TTS模型配置");
        }

        AiModelProvider provider = getProvider(model.getProviderId());
        if (provider == null) {
            throw new RuntimeException("未找到TTS模型对应的提供商配置");
        }

        if (provider != null && "dashscope".equals(provider.getProviderType())) {
            return synthesizeWithDashScope(provider, model, dto);
        }

        String apiKey = resolveApiKey(provider);
        String baseUrl = provider.getApiBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_TTS_URL;
        }
        String apiUrl;
        if (baseUrl.contains("/v1/audio/speech")) {
            apiUrl = baseUrl;
        } else if (baseUrl.endsWith("/v1") || baseUrl.endsWith("/v1/")) {
            apiUrl = baseUrl.replaceAll("/+$", "") + "/audio/speech";
        } else {
            apiUrl = baseUrl.replaceAll("/+$", "") + "/v1/audio/speech";
        }

        String voice = dto.getVoice() != null ? dto.getVoice() : (model.getDefaultVoice() != null ? model.getDefaultVoice() : "alloy");
        double speed = dto.getSpeed() != null ? dto.getSpeed() : 1.0;
        String modelName = model.getModelName() != null ? model.getModelName() : "tts-1";

        JSONObject requestBody = JSONUtil.createObj()
                .set("model", modelName)
                .set("input", dto.getText())
                .set("voice", voice)
                .set("response_format", "mp3")
                .set("speed", speed);

        try {
            // 大文本分段处理：OpenAI TTS 单次输入上限约 4096 字符，分段合成后拼接 mp3 字节
            List<String> segments = TextSegmenter.segment(dto.getText(), OPENAI_TTS_MAX_CHARS);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (String segment : segments) {
                buffer.write(requestOpenAiTts(apiUrl, apiKey, modelName, voice, speed, segment));
            }
            byte[] audioBytes = buffer.toByteArray();

            String audioUrl = saveAudioFile(audioBytes);

            AiTtsRecord record = new AiTtsRecord();
            record.setText(dto.getText());
            record.setModelName(modelName);
            record.setVoice(voice);
            record.setAudioUrl(audioUrl);
            record.setFileSize(BigInteger.valueOf(audioBytes.length));
            record.setDurationSeconds(null);
            record.setCost(BigDecimal.ZERO);
            lightDao.save(record);

            return audioBytes;
        } catch (Exception e) {
            log.error("TTS合成失败", e);
            throw new RuntimeException("TTS合成失败: " + e.getMessage());
        }
    }

    /**
     * 调用 OpenAI 兼容 TTS 接口合成单个文本片段，返回 mp3 字节。
     */
    private byte[] requestOpenAiTts(String apiUrl, String apiKey, String modelName,
                                    String voice, double speed, String text) {
        JSONObject requestBody = JSONUtil.createObj()
                .set("model", modelName)
                .set("input", text)
                .set("voice", voice)
                .set("response_format", "mp3")
                .set("speed", speed);
        return HttpUtil.createPost(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(60000)
                .execute()
                .bodyBytes();
    }

    private byte[] synthesizeWithDashScope(AiModelProvider provider, AiModel model, TtsSynthesizeDTO dto) {
        String apiKey = resolveApiKey(provider);
        String rawBase = provider.getApiBaseUrl();
        if (rawBase == null || rawBase.isBlank()) {
            rawBase = "https://dashscope.aliyuncs.com";
        }
        rawBase = rawBase.replaceAll("/+$", "").replaceFirst("/api/v1$", "");
        String apiUrl = rawBase + "/api/v1/services/audio/tts/SpeechSynthesizer";

        String voice = model.getDefaultVoice() != null ? model.getDefaultVoice() : (dto.getVoice() != null ? dto.getVoice() : "longxiaochun");
        String modelName = model.getModelName() != null ? model.getModelName() : "cosyvoice-v3-flash";

        JSONObject input = new JSONObject();
        input.set("text", dto.getText());
        input.set("voice", voice);
        input.set("format", "wav");
        input.set("sample_rate", 24000);

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", modelName);
        requestBody.set("input", input);

        log.info("DashScope TTS request URL: {}, model: {}, voice: {}, textLen: {}",
                apiUrl, modelName, voice, dto.getText().length());

        try {
            byte[] responseBytes = HttpUtil.createPost(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(120000)
                    .execute()
                    .bodyBytes();

            String bodyStr = new String(responseBytes, StandardCharsets.UTF_8);
            if (bodyStr.trim().startsWith("{")) {
                JSONObject errJson = JSONUtil.parseObj(bodyStr);
                String errMsg = errJson.containsKey("error")
                        ? errJson.getJSONObject("error").getStr("message")
                        : errJson.getStr("message", "未知错误");
                String suggestion = "";
                if (errMsg.contains("does not support http call") && rawBase.contains("dashscope.aliyuncs.com")) {
                    suggestion = "访问点 " + rawBase + " 仅支持SSE流式调用，请在模型供应商配置中设置正确的MaaS接入点URL，格式如：https://{WorkspaceId}.cn-beijing.maas.aliyuncs.com";
                } else if (errMsg.contains("url error")) {
                    suggestion = "请检查TTS模型配置中的模型名称是否正确，推荐使用 cosyvoice-v3-flash；同时确认MaaS接入点URL格式为 https://{WorkspaceId}.{region}.maas.aliyuncs.com";
                } else if (errMsg.contains("418")) {
                    suggestion = "当前音色与模型版本不匹配。cosyvoice-v3.5-flash/v3.5-plus 没有系统音色，仅支持自定义克隆音色；请使用 cosyvoice-v3-flash（支持 longxiaochun 等系统音色）；请在TTS模型配置中设置正确的模型名称和默认音色";
                }
                throw new RuntimeException("DashScope TTS API错误: " + errMsg + (suggestion.isEmpty() ? "" : "。提示: " + suggestion));
            }

            byte[] audioBytes = responseBytes;

            log.info("DashScope TTS response size: {} bytes", audioBytes.length);

            String audioUrl = saveAudioFile(audioBytes);

            AiTtsRecord record = new AiTtsRecord();
            record.setText(dto.getText());
            record.setModelName(modelName);
            record.setVoice(voice);
            record.setAudioUrl(audioUrl);
            record.setFileSize(BigInteger.valueOf(audioBytes.length));
            record.setDurationSeconds(null);
            record.setCost(BigDecimal.ZERO);
            lightDao.save(record);

            return audioBytes;
        } catch (Exception e) {
            log.error("DashScope TTS合成失败", e);
            throw new RuntimeException("TTS合成失败: " + e.getMessage());
        }
    }

    private String saveAudioFile(byte[] audioBytes) {
        try {
            File dir = new File(AUDIO_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = IdUtil.fastSimpleUUID() + ".mp3";
            Path filePath = Path.of(AUDIO_DIR + fileName);
            Files.write(filePath, audioBytes);
            return "/" + AUDIO_DIR + fileName;
        } catch (Exception e) {
            log.warn("保存音频文件失败，将返回原始数据", e);
            return null;
        }
    }

    @Override
    public List<AiTtsModelVO> listModels() {
        EntityQuery eq = EntityQuery.create()
                .where("model_type = 'tts' and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        return lightDao.findEntity(AiModel.class, eq).stream()
                .map(m -> {
                    AiModelProvider p = getProvider(m.getProviderId());
                    AiTtsModelVO vo = new AiTtsModelVO();
                    vo.setId(m.getId());
                    String providerType = p != null ? p.getProviderType() : null;
                    vo.setProviderType(providerType);
                    vo.setVoices(PROVIDER_VOICES.getOrDefault(providerType, List.of()));
                    vo.setModelName(m.getModelName());
                    vo.setApiKey(p != null ? ApiKeyCipher.mask(ApiKeyCipher.decrypt(p.getApiKey())) : null);
                    vo.setApiBaseUrl(p != null ? p.getApiBaseUrl() : null);
                    vo.setDefaultVoice(m.getDefaultVoice());
                    vo.setIsDefault(m.getIsDefault());
                    vo.setEnableStatus(m.getEnableStatus());
                    vo.setSort(m.getSort());
                    vo.setRemark(m.getRemark());
                    return vo;
                }).toList();
    }

    @Override
    public boolean add(AiTtsModelAddDTO dto) {
        AiModel model = BeanUtil.copyProperties(dto, AiModel.class);
        model.setModelType("tts");
        if (dto.getProviderType() != null) {
            AiModelProvider provider = lightDao.findEntity(
                            AiModelProvider.class,
                            EntityQuery.create()
                                    .where("provider_type = ? and is_deleted = 0")
                                    .values(dto.getProviderType())
                                    .orderByDesc("sort"))
                    .stream().findFirst().orElse(null);
            if (provider != null) {
                model.setProviderId(provider.getId());
            }
        }
        return lightDao.save(model) != null;
    }

    @Override
    public boolean update(AiTtsModelUpdateDTO dto) {
        AiModel model = BeanUtil.copyProperties(dto, AiModel.class);
        if (dto.getProviderType() != null) {
            AiModelProvider provider = lightDao.findEntity(
                            AiModelProvider.class,
                            EntityQuery.create()
                                    .where("provider_type = ? and is_deleted = 0")
                                    .values(dto.getProviderType())
                                    .orderByDesc("sort"))
                    .stream().findFirst().orElse(null);
            if (provider != null) {
                model.setProviderId(provider.getId());
            }
        }
        Long updated = lightDao.update(model);
        return updated != null && updated > 0;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> ids) {
        if (ids == null || ids.isEmpty()) return false;
        for (BigInteger id : ids) {
            AiModel model = new AiModel(id);
            model.setIsDeleted(1);
            lightDao.update(model);
        }
        return true;
    }

    @Override
    public ApiPage<AiTtsModelVO> listByPage(AiTtsModelPageDTO pageDTO) {
        Map<String, Object> params = MapUtil.newHashMap(3);
        AiTtsModelPageDTO.Query q = pageDTO.getTtsModel();
        params.put("providerType", q != null ? q.getProviderType() : null);
        params.put("modelName", q != null ? q.getModelName() : null);
        params.put("enableStatus", q != null ? q.getEnableStatus() : null);
        Page<AiTtsModelVO> pageParam = new Page<>();
        pageParam.setPageNo(pageDTO.getPageNo());
        pageParam.setPageSize(pageDTO.getPageSize());
        Page<AiTtsModelVO> page = lightDao.findPage(pageParam, "ai_tts_model_findList", params, AiTtsModelVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public ApiPage<AiTtsRecordVO> listHistoryByPage(ApiPageDTO pageDTO) {
        Page<AiTtsRecordVO> pageParam = new Page<>();
        pageParam.setPageNo(pageDTO.getPageNo());
        pageParam.setPageSize(pageDTO.getPageSize());
        Map<String, Object> params = new HashMap<>();
        Page<AiTtsRecordVO> page = lightDao.findPage(pageParam, "ai_tts_record_findList", params, AiTtsRecordVO.class);
        return ApiPage.rest(page);
    }

    private AiModel getDefaultModel() {
        EntityQuery query = EntityQuery.create()
                .where("model_type = 'tts' and is_default = 1 and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        List<AiModel> models = lightDao.findEntity(AiModel.class, query);
        if (models.isEmpty()) {
            query = EntityQuery.create()
                    .where("model_type = 'tts' and enable_status = 1 and is_deleted = 0")
                    .orderByDesc("sort");
            models = lightDao.findEntity(AiModel.class, query);
        }
        return models.isEmpty() ? null : models.getFirst();
    }

    private AiModelProvider getProvider(BigInteger providerId) {
        if (providerId == null) return null;
        return lightDao.load(new AiModelProvider(providerId));
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
}
