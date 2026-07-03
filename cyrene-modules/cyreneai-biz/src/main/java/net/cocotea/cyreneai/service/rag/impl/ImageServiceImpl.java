package net.cocotea.cyreneai.service.rag.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.dto.AiImageModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiImageModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiImageModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.ImageGenerateDTO;
import net.cocotea.cyreneai.model.po.AiImageRecord;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.model.vo.AiImageModelVO;
import net.cocotea.cyreneai.model.vo.AiImageRecordVO;
import net.cocotea.cyreneai.service.rag.ImageService;
import net.cocotea.cyreneadmin.model.ApiPage;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ImageServiceImpl implements ImageService {

    @Db
    private LightDao lightDao;

    private AiModelProvider getProvider(BigInteger providerId) {
        if (providerId == null) return null;
        return lightDao.load(new AiModelProvider(providerId));
    }

    @Override
    public String generate(ImageGenerateDTO dto) {
        AiModel model;
        if (dto.getModelId() != null) {
            model = lightDao.load(new AiModel(dto.getModelId()));
        } else {
            model = getDefaultImageModel();
        }
        if (model == null) {
            return "错误: 未找到可用图片生成模型，请先配置模型";
        }

        AiModelProvider provider = getProvider(model.getProviderId());
        if (provider == null) {
            return "错误: 模型未关联有效的提供商";
        }

        String apiKey = provider.getApiKey();
        String baseUrl = provider.getApiBaseUrl();
        String providerType = provider.getProviderType();

        if (apiKey == null || apiKey.isBlank()) {
            return "错误: 模型未配置API密钥";
        }

        String modelName = model.getModelName();
        String size = dto.getSize() != null ? dto.getSize() : (model.getDefaultSize() != null ? model.getDefaultSize() : "1024x1024");
        int n = dto.getN() != null ? dto.getN() : 1;

        return switch (providerType.toLowerCase()) {
            case "openai" -> generateWithOpenAI(apiKey, baseUrl, modelName, dto.getPrompt(), size, n, dto.getStyle(), model.getModelName());
            case "stability" -> generateWithStability(apiKey, baseUrl, modelName, dto.getPrompt(), size, n);
            case "dashscope" -> generateWithDashScope(apiKey, baseUrl, modelName, dto.getPrompt(), size, n);
            default -> generateWithMultimodal(apiKey, baseUrl, modelName, dto.getPrompt(), size, model.getModelName());
        };
    }

    private String generateWithOpenAI(String apiKey, String baseUrl, String modelName, String prompt, String size, int n, String style, String dbModelName) {
        String url = (baseUrl != null && !baseUrl.isBlank() ? baseUrl : "https://api.openai.com") + "/v1/images/generations";
        JSONObject requestBody = JSONUtil.createObj()
                .set("model", modelName != null ? modelName : "dall-e-3")
                .set("prompt", prompt)
                .set("n", n)
                .set("size", size);
        if (style != null && !style.isBlank()) {
            requestBody.set("style", style);
        }

        try {
            String response = HttpUtil.createPost(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(60000)
                    .execute()
                    .body();

            if (!response.startsWith("{")) {
                log.warn("OpenAI API returned non-JSON response: {}", response);
                return "图片生成失败: " + response;
            }

            JSONObject json = JSONUtil.parseObj(response);
            if (json.containsKey("error")) {
                log.warn("OpenAI API returned error: {}", json.get("error"));
                return "图片生成失败: " + json.getJSONObject("error").getStr("message", response);
            }

            var data = json.getJSONArray("data");
            if (data != null && !data.isEmpty()) {
                var item = data.getJSONObject(0);
                String imageUrl = item.getStr("url");
                String revisedPrompt = item.getStr("revised_prompt");

                AiImageRecord record = new AiImageRecord();
                record.setPrompt(prompt);
                record.setRevisedPrompt(revisedPrompt);
                record.setModelName(dbModelName);
                record.setImageUrl(imageUrl);
                record.setImageSize(size);
                record.setStyle(style);
                record.setCreateTime(LocalDateTime.now());
                lightDao.save(record);

                return imageUrl;
            }
            return "图片生成失败: " + response;
        } catch (Exception e) {
            log.error("OpenAI image generation failed", e);
            return "图片生成失败: " + e.getMessage();
        }
    }

    private String generateWithStability(String apiKey, String baseUrl, String modelName, String prompt, String size, int n) {
        String url = (baseUrl != null && !baseUrl.isBlank() ? baseUrl : "https://api.stability.ai") + "/v2beta/stable-image/generate/sd3";
        int width = 1024;
        int height = 1024;
        if (size != null && size.contains("x")) {
            String[] parts = size.split("x");
            try {
                width = Integer.parseInt(parts[0]);
                height = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }

        try {
            String response = HttpUtil.createPost(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.createObj()
                            .set("prompt", prompt)
                            .set("width", width)
                            .set("height", height)
                            .set("samples", n)
                            .toString())
                    .timeout(60000)
                    .execute()
                    .body();

            JSONObject json = JSONUtil.parseObj(response);
            var artifacts = json.getJSONArray("artifacts");
            if (artifacts != null && !artifacts.isEmpty()) {
                String base64Image = artifacts.getJSONObject(0).getStr("base64");
                String imageUrl = "data:image/png;base64," + base64Image;

                AiImageRecord record = new AiImageRecord();
                record.setPrompt(prompt);
                record.setModelName(modelName != null ? modelName : "stable-diffusion-3");
                record.setImageUrl(imageUrl);
                record.setImageSize(size);
                record.setCreateTime(LocalDateTime.now());
                lightDao.save(record);

                return imageUrl;
            }
            return "图片生成失败: " + response;
        } catch (Exception e) {
            log.error("Stability AI image generation failed", e);
            return "图片生成失败: " + e.getMessage();
        }
    }

    private String generateWithDashScope(String apiKey, String baseUrl, String modelName, String prompt, String size, int n) {
        String dashScopeBase = baseUrl != null && !baseUrl.isBlank() ? baseUrl : "https://dashscope.aliyuncs.com";
        if (dashScopeBase.endsWith("/")) {
            dashScopeBase = dashScopeBase.substring(0, dashScopeBase.length() - 1);
        }
        try {
            String url;
            JSONObject requestBody;
            String dashScopeSize = size != null ? size.replace("x", "*") : "1024*1024";

            if (modelName != null && modelName.startsWith("qwen-image")) {
                String apiPath = "/api/v1/services/aigc/multimodal-generation/generation";
                url = dashScopeBase.endsWith("/api/v1") ? dashScopeBase + apiPath.replace("/api/v1", "") : dashScopeBase + apiPath;

                JSONArray content = JSONUtil.createArray();
                content.add(JSONUtil.createObj().set("text", prompt));

                JSONObject msg = new JSONObject();
                msg.set("role", "user");
                msg.set("content", content);

                JSONArray messages = JSONUtil.createArray();
                messages.add(msg);

                JSONObject input = new JSONObject();
                input.set("messages", messages);

                JSONObject params = new JSONObject();
                params.set("size", dashScopeSize);
                params.set("n", n);

                requestBody = new JSONObject();
                requestBody.set("model", modelName);
                requestBody.set("input", input);
                requestBody.set("parameters", params);
            } else {
                String apiPath = "/api/v1/services/aigc/text2image/image-synthesis";
                url = dashScopeBase.endsWith("/api/v1") ? dashScopeBase + apiPath.replace("/api/v1", "") : dashScopeBase + apiPath;
                requestBody = new JSONObject();
                requestBody.set("model", modelName);
                requestBody.set("input", new JSONObject().set("prompt", prompt));
                requestBody.set("parameters", new JSONObject()
                        .set("size", size != null ? size : "1024x1024")
                        .set("n", n));
            }

            log.info("DashScope request URL: {}", url);
            log.info("DashScope request body: {}", requestBody);

            String response = HttpUtil.createPost(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(120000)
                    .execute()
                    .body();

            if (!response.startsWith("{")) {
                log.warn("DashScope API returned non-JSON: {}", response);
                return "图片生成失败: " + response;
            }

            JSONObject json = JSONUtil.parseObj(response);
            log.info("DashScope response: {}", json);

            if (json.containsKey("code") && !"200".equals(json.getStr("code"))) {
                return "图片生成失败: " + json.getStr("message", response);
            }

            JSONObject output = json.getJSONObject("output");
            if (output == null) {
                return "图片生成失败: " + response;
            }

            String taskStatus = output.getStr("task_status");
            if ("FAILED".equals(taskStatus)) {
                return "图片生成失败: " + output.getStr("message", "任务失败");
            }

            if ("SUCCEEDED".equals(taskStatus) || taskStatus == null) {
                var results = output.getJSONArray("results");
                if (results != null && !results.isEmpty()) {
                    String imageUrl = results.getJSONObject(0).getStr("url");
                    if (imageUrl != null) {
                        AiImageRecord record = new AiImageRecord();
                        record.setPrompt(prompt);
                        record.setModelName(modelName);
                        record.setImageUrl(imageUrl);
                        record.setImageSize(size);
                        record.setCreateTime(LocalDateTime.now());
                        lightDao.save(record);
                        return imageUrl;
                    }
                }

                var choices = output.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    var message = choices.getJSONObject(0).getJSONObject("message");
                    if (message != null) {
                        var content = message.getJSONArray("content");
                        if (content != null) {
                            for (int i = 0; i < content.size(); i++) {
                                String img = content.getJSONObject(i).getStr("image");
                                if (img != null) {
                                    AiImageRecord record = new AiImageRecord();
                                    record.setPrompt(prompt);
                                    record.setModelName(modelName);
                                    record.setImageUrl(img);
                                    record.setImageSize(size);
                                    record.setCreateTime(LocalDateTime.now());
                                    lightDao.save(record);
                                    return img;
                                }
                            }
                        }
                    }
                }
            }
            return "图片生成失败: " + response;
        } catch (Exception e) {
            log.error("DashScope image generation failed, URL: {}", dashScopeBase, e);
            return "图片生成失败: " + e.getMessage();
        }
    }

    private String generateWithMultimodal(String apiKey, String baseUrl, String modelName, String prompt, String size, String dbModelName) {
        String url = (baseUrl != null && !baseUrl.isBlank() ? baseUrl : "https://api.openai.com") + "/v1/chat/completions";
        try {
            JSONObject requestBody = JSONUtil.createObj()
                    .set("model", modelName != null ? modelName : "gpt-4o")
                    .set("messages", JSONUtil.createArray()
                            .add(JSONUtil.createObj()
                                    .set("role", "user")
                                    .set("content", "Generate an image: " + prompt + ". Return only the URL of the generated image.")))
                    .set("max_tokens", 1000);

            String response = HttpUtil.createPost(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(60000)
                    .execute()
                    .body();

            if (!response.startsWith("{")) {
                log.warn("Multimodal API returned non-JSON response: {}", response);
                return "图片生成失败: " + response;
            }

            JSONObject json = JSONUtil.parseObj(response);
            if (json.containsKey("error")) {
                log.warn("Multimodal API returned error: {}", json.get("error"));
                return "图片生成失败: " + json.getJSONObject("error").getStr("message", response);
            }

            var choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                String content = choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");

                AiImageRecord record = new AiImageRecord();
                record.setPrompt(prompt);
                record.setModelName(dbModelName != null ? dbModelName : modelName);
                record.setImageUrl(content);
                record.setImageSize(size);
                record.setCreateTime(LocalDateTime.now());
                lightDao.save(record);

                return content;
            }
            return "图片生成失败: " + response;
        } catch (Exception e) {
            log.error("Multimodal image generation failed", e);
            return "图片生成失败: " + e.getMessage();
        }
    }

    private AiModel getDefaultImageModel() {
        EntityQuery query = EntityQuery.create()
                .where("is_default = 1 and enable_status = 1 and is_deleted = 0 and model_type = 'image'")
                .orderByDesc("sort");
        List<AiModel> models = lightDao.findEntity(AiModel.class, query);
        if (models.isEmpty()) {
            query = EntityQuery.create()
                    .where("enable_status = 1 and is_deleted = 0 and model_type = 'image'")
                    .orderByDesc("sort");
            models = lightDao.findEntity(AiModel.class, query);
        }
        return models.isEmpty() ? null : models.getFirst();
    }

    @Override
    public List<AiImageModelVO> listModels() {
        EntityQuery eq = EntityQuery.create()
                .where("enable_status = 1 and is_deleted = 0 and model_type = 'image'")
                .orderByDesc("sort");
        return lightDao.findEntity(AiModel.class, eq).stream()
                .map(m -> {
                    AiImageModelVO vo = new AiImageModelVO();
                    vo.setId(m.getId());
                    AiModelProvider provider = getProvider(m.getProviderId());
                    if (provider != null) {
                        vo.setProviderType(provider.getProviderType());
                        vo.setApiKey(provider.getApiKey());
                        vo.setApiBaseUrl(provider.getApiBaseUrl());
                    }
                    vo.setModelName(m.getModelName());
                    vo.setDefaultSize(m.getDefaultSize());
                    vo.setIsDefault(m.getIsDefault());
                    vo.setEnableStatus(m.getEnableStatus());
                    vo.setSort(m.getSort());
                    vo.setRemark(m.getRemark());
                    return vo;
                }).toList();
    }

    @Override
    public boolean add(AiImageModelAddDTO dto) {
        AiModel model = BeanUtil.copyProperties(dto, AiModel.class);
        model.setModelType("image");
        if (dto.getProviderType() != null) {
            List<AiModelProvider> providers = lightDao.findEntity(AiModelProvider.class,
                    EntityQuery.create().where("provider_type = ?").values(dto.getProviderType()));
            if (!providers.isEmpty()) {
                model.setProviderId(providers.getFirst().getId());
            }
        }
        return lightDao.save(model) != null;
    }

    @Override
    public boolean update(AiImageModelUpdateDTO dto) {
        AiModel model = BeanUtil.copyProperties(dto, AiModel.class);
        if (dto.getProviderType() != null) {
            List<AiModelProvider> providers = lightDao.findEntity(AiModelProvider.class,
                    EntityQuery.create().where("provider_type = ?").values(dto.getProviderType()));
            if (!providers.isEmpty()) {
                model.setProviderId(providers.getFirst().getId());
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
    public ApiPage<AiImageModelVO> listByPage(AiImageModelPageDTO pageDTO) {
        Map<String, Object> params = MapUtil.newHashMap(3);
        AiImageModelPageDTO.Query q = pageDTO.getImageModel();
        params.put("providerType", q != null ? q.getProviderType() : null);
        params.put("modelName", q != null ? q.getModelName() : null);
        params.put("enableStatus", q != null ? q.getEnableStatus() : null);
        Page<AiImageModelVO> pageParam = new Page<>();
        pageParam.setPageNo(pageDTO.getPageNo());
        pageParam.setPageSize(pageDTO.getPageSize());
        Page<AiImageModelVO> page = lightDao.findPage(pageParam, "ai_image_model_findList", params, AiImageModelVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public ApiPage<AiImageRecordVO> listHistoryByPage(AiImageModelPageDTO pageDTO) {
        Page<AiImageRecordVO> pageParam = new Page<>();
        pageParam.setPageNo(pageDTO.getPageNo());
        pageParam.setPageSize(pageDTO.getPageSize());
        Map<String, Object> params = new HashMap<>(1);
        Page<AiImageRecordVO> page = lightDao.findPage(pageParam, "ai_image_record_findList", params, AiImageRecordVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean deleteHistory(BigInteger id) {
        Long updated = lightDao.delete(new AiImageRecord(id));
        return updated != null && updated > 0;
    }

}
