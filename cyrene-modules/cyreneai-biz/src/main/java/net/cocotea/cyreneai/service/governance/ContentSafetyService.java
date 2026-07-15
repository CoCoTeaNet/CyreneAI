package net.cocotea.cyreneai.service.governance;

/**
 * 内容安全检查服务
 */
public interface ContentSafetyService {

    /**
     * 检查文本内容
     * @param text 待检查文本
     * @param target input 输入 / output 输出
     * @return 检查结果; 若命中 replace, sanitizedText 为替换后文本
     */
    ContentSafetyResult check(String text, String target);
}
