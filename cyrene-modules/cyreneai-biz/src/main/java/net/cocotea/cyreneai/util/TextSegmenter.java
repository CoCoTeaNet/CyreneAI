package net.cocotea.cyreneai.util;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 大文本分段处理工具。
 * <p>
 * 将长文本切分为不超过 {@code maxChars} 的片段，优先在段落、句子边界处断开，
 * 无合适边界时按长度硬切分，用于规避下游模型/接口的输入长度上限（如 TTS 输入限制）。
 *
 * @author cyrene
 */
public final class TextSegmenter {

    /** 句子结束符（中英文）。 */
    private static final char[] SENTENCE_ENDS = {'。', '！', '？', '!', '?', '.', ';', '；', '\n'};

    private TextSegmenter() {
    }

    /**
     * 按最大长度切分文本。
     *
     * @param text     原始文本
     * @param maxChars 单段最大字符数（需 > 0）
     * @return 切分后的片段列表；文本为空返回空列表
     */
    public static List<String> segment(String text, int maxChars) {
        List<String> result = new ArrayList<>();
        if (StrUtil.isBlank(text) || maxChars <= 0) {
            return result;
        }
        if (text.length() <= maxChars) {
            result.add(text);
            return result;
        }

        StringBuilder current = new StringBuilder();
        // 先按段落切分，再逐段落归并
        for (String paragraph : text.split("\n")) {
            if (paragraph.isEmpty()) {
                continue;
            }
            for (String sentence : splitLongUnit(paragraph, maxChars)) {
                if (current.length() + sentence.length() + 1 > maxChars && current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                if (!current.isEmpty()) {
                    current.append('\n');
                }
                current.append(sentence);
            }
        }
        if (!current.isEmpty()) {
            result.add(current.toString());
        }
        return result;
    }

    /**
     * 将超过上限的段落按句子边界切分；句子仍超限时按长度硬切分。
     */
    private static List<String> splitLongUnit(String paragraph, int maxChars) {
        List<String> units = new ArrayList<>();
        if (paragraph.length() <= maxChars) {
            units.add(paragraph);
            return units;
        }
        int start = 0;
        while (start < paragraph.length()) {
            int end = Math.min(start + maxChars, paragraph.length());
            if (end < paragraph.length()) {
                int boundary = lastSentenceBoundary(paragraph, start, end);
                if (boundary > start) {
                    end = boundary;
                }
            }
            units.add(paragraph.substring(start, end));
            start = end;
        }
        return units;
    }

    /**
     * 在 [start, end) 内查找最后一个句子结束符的位置（含该符号，返回其后一位）。
     */
    private static int lastSentenceBoundary(String text, int start, int end) {
        for (int i = end - 1; i > start; i--) {
            char c = text.charAt(i);
            for (char delim : SENTENCE_ENDS) {
                if (c == delim) {
                    return i + 1;
                }
            }
        }
        return -1;
    }
}
