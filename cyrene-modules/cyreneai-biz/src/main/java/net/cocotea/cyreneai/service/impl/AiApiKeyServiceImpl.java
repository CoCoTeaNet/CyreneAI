package net.cocotea.cyreneai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiApiKeyAddDTO;
import net.cocotea.cyreneai.model.dto.AiApiKeyPageDTO;
import net.cocotea.cyreneai.model.dto.AiApiKeyUpdateDTO;
import net.cocotea.cyreneai.model.po.AiApiKey;
import net.cocotea.cyreneai.model.po.AiApiKeyUsageDaily;
import net.cocotea.cyreneai.model.vo.AiApiKeyUsageVO;
import net.cocotea.cyreneai.model.vo.AiApiKeyVO;
import net.cocotea.cyreneai.service.AiApiKeyService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiApiKeyServiceImpl implements AiApiKeyService {

    private static final String KEY_PLAIN_PREFIX = "sk-cyr-";

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiApiKeyAddDTO param) {
        generate(param);
        return true;
    }

    @Override
    public AiApiKeyVO generate(AiApiKeyAddDTO param) {
        AiApiKey key = lightDao.convertType(param, AiApiKey.class);
        BigInteger userId = currentUserId();
        key.setUserId(userId);
        String plain = KEY_PLAIN_PREFIX + IdUtil.simpleUUID();
        key.setKeyHash(DigestUtil.sha256Hex(plain));
        key.setKeyPrefix(plain.substring(0, Math.min(14, plain.length())) + "****");
        if (key.getEnableStatus() == null) key.setEnableStatus(1);
        if (key.getSort() == null) key.setSort(0);
        if (key.getTokensUsedThisMonth() == null) key.setTokensUsedThisMonth(0L);
        key.setQuotaResetTime(nextMonthStart());
        Object saved = lightDao.save(key);
        AiApiKeyVO vo = lightDao.convertType(key, AiApiKeyVO.class);
        vo.setPlainKey(plain);
        return saved != null ? vo : null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiApiKeyUpdateDTO param) {
        AiApiKey key = lightDao.convertType(param, AiApiKey.class);
        // 明确不允许通过 update 覆盖敏感字段
        key.setKeyHash(null);
        key.setKeyPrefix(null);
        key.setUserId(null);
        key.setTokensUsedThisMonth(null);
        key.setQuotaResetTime(null);
        Long update = lightDao.update(key);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiApiKeyVO> listByPage(AiApiKeyPageDTO pageDTO) {
        AiApiKeyPageDTO.Query query = pageDTO.getAiApiKey();
        Map<String, Object> map = MapUtil.newHashMap(4);
        map.put("name", query != null ? query.getName() : null);
        map.put("userId", query != null ? query.getUserId() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiApiKeyVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_api_key_findList", map, AiApiKeyVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiApiKey key = new AiApiKey().setId(id).setIsDeleted(1);
        Long update = lightDao.update(key);
        return update != null && update > 0;
    }

    @Override
    public AiApiKey verifyPlainKey(String plainKey) {
        if (StrUtil.isBlank(plainKey)) return null;
        String hash = DigestUtil.sha256Hex(plainKey);
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("keyHash", hash);
        List<AiApiKey> list = lightDao.find("ai_api_key_findByHash", map, AiApiKey.class);
        if (list.isEmpty()) return null;
        AiApiKey key = list.getFirst();
        if (key.getEnableStatus() != null && key.getEnableStatus() != 1) return null;
        if (key.getExpireTime() != null && key.getExpireTime().isBefore(LocalDateTime.now())) return null;
        return key;
    }

    @Override
    public void recordUsage(BigInteger apiKeyId, BigInteger userId, int promptTokens, int completionTokens,
                            BigDecimal cost, String status) {
        try {
            // 累加当日聚合
            LocalDate today = LocalDate.now();
            Map<String, Object> qmap = MapUtil.newHashMap(2);
            qmap.put("apiKeyId", apiKeyId);
            qmap.put("statDate", today);
            List<AiApiKeyUsageDaily> exist = lightDao.find("ai_api_key_usage_findByKeyDate", qmap, AiApiKeyUsageDaily.class);
            AiApiKeyUsageDaily row;
            boolean isNew = exist.isEmpty();
            if (isNew) {
                row = new AiApiKeyUsageDaily().setApiKeyId(apiKeyId).setUserId(userId).setStatDate(today)
                        .setRequestCount(0).setSuccessCount(0).setBlockedCount(0).setErrorCount(0)
                        .setPromptTokens(0L).setCompletionTokens(0L).setTotalTokens(0L).setCost(BigDecimal.ZERO)
                        .setCreateTime(LocalDateTime.now());
            } else {
                row = exist.getFirst();
                row.setUpdateTime(LocalDateTime.now());
            }
            row.setRequestCount(row.getRequestCount() + 1);
            switch (status) {
                case "success" -> row.setSuccessCount(row.getSuccessCount() + 1);
                case "blocked", "quota_exceeded", "rate_limited" -> row.setBlockedCount(row.getBlockedCount() + 1);
                case "error" -> row.setErrorCount(row.getErrorCount() + 1);
                default -> { /* other status ignored */ }
            }
            row.setPromptTokens(row.getPromptTokens() + promptTokens);
            row.setCompletionTokens(row.getCompletionTokens() + completionTokens);
            row.setTotalTokens(row.getTotalTokens() + promptTokens + completionTokens);
            if (cost != null) row.setCost(row.getCost().add(cost));
            if (isNew) lightDao.save(row); else lightDao.update(row);

            // 累加 Key 本月已用 Token 并刷新 last_used_time
            AiApiKey update = new AiApiKey(apiKeyId);
            AiApiKey db = lightDao.load(new AiApiKey(apiKeyId));
            if (db != null) {
                long used = db.getTokensUsedThisMonth() == null ? 0L : db.getTokensUsedThisMonth();
                update.setTokensUsedThisMonth(used + promptTokens + completionTokens);
                update.setLastUsedTime(LocalDateTime.now());
                lightDao.update(update);
            }
        } catch (Exception e) {
            log.error("record api key usage failed, apiKeyId={}", apiKeyId, e);
        }
    }

    @Override
    public List<AiApiKeyUsageVO> statRecent(BigInteger apiKeyId, Integer days) {
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("apiKeyId", apiKeyId);
        map.put("startDate", LocalDate.now().minusDays(days == null ? 30 : days));
        return lightDao.find("ai_api_key_usage_statRecent", map, AiApiKeyUsageVO.class);
    }

    private BigInteger currentUserId() {
        try {
            Object id = StpUtil.getLoginId();
            return new BigInteger(id.toString());
        } catch (Exception e) {
            return BigInteger.ZERO;
        }
    }

    private LocalDateTime nextMonthStart() {
        LocalDate first = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        return first.atStartOfDay();
    }
}
