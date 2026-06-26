package net.cocotea.cyreneai.service.rag;

import java.util.ArrayList;
import java.util.List;

public class TextSplitter {

    public static List<String> split(String text, String strategy, int chunkSize, int overlap) {
        return switch (strategy != null ? strategy : "paragraph") {
            case "size" -> splitBySize(text, chunkSize, overlap);
            case "recursive" -> splitRecursive(text, chunkSize, overlap);
            default -> splitByParagraph(text, chunkSize, overlap);
        };
    }

    public static List<String> splitBySize(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += chunkSize - overlap;
        }
        return chunks;
    }

    public static List<String> splitByParagraph(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder current = new StringBuilder();

        for (String para : paragraphs) {
            String trimmed = para.strip();
            if (trimmed.isEmpty()) continue;
            if (current.length() + trimmed.length() > chunkSize && !current.isEmpty()) {
                chunks.add(current.toString().strip());
                String overlapText = getOverlapText(current.toString(), overlap);
                current = new StringBuilder(overlapText);
            }
            if (current.isEmpty()) {
                current.append(trimmed);
            } else {
                current.append("\n\n").append(trimmed);
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().strip());
        }
        return chunks;
    }

    public static List<String> splitRecursive(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        List<String> sections = splitBySeparators(text);
        StringBuilder current = new StringBuilder();

        for (String section : sections) {
            if (section.length() > chunkSize) {
                if (!current.isEmpty()) {
                    chunks.add(current.toString().strip());
                    current = new StringBuilder();
                }
                chunks.addAll(splitBySize(section, chunkSize, overlap));
                continue;
            }
            if (current.length() + section.length() > chunkSize && !current.isEmpty()) {
                chunks.add(current.toString().strip());
                String overlapText = getOverlapText(current.toString(), overlap);
                current = new StringBuilder(overlapText);
            }
            if (current.isEmpty()) {
                current.append(section);
            } else {
                current.append("\n").append(section);
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().strip());
        }
        return chunks;
    }

    private static List<String> splitBySeparators(String text) {
        List<String> result = new ArrayList<>();
        String[] byDoubleNewline = text.split("\\n\\s*\\n");
        for (String section : byDoubleNewline) {
            if (section.length() > 1000) {
                String[] byNewline = section.split("\\n");
                for (String line : byNewline) {
                    String trimmed = line.strip();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            } else {
                String trimmed = section.strip();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    private static String getOverlapText(String text, int overlapChars) {
        if (overlapChars <= 0 || text.length() <= overlapChars) return "";
        int end = text.length();
        int start = Math.max(0, end - overlapChars);
        int newlinePos = text.indexOf('\n', start);
        if (newlinePos >= 0 && newlinePos < end) {
            return text.substring(newlinePos);
        }
        int spacePos = text.indexOf(' ', start);
        if (spacePos >= 0 && spacePos < end) {
            return text.substring(spacePos + 1);
        }
        return text.substring(start);
    }
}
