package org.example.utils;

import org.example.repositories.ChunkSearchResult;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TextUtils {
    private static final String REGEX_WHITESPACE = "\\s+";
    private static final String SENTENCE_DOT_SPACE = ". ";
    private static final int DOT_OFFSET = 1;

    private TextUtils() {}

    public static int calculateTokenCount(String content) {
        if (content == null || content.isBlank()) return 0;
        return content.trim().split(REGEX_WHITESPACE).length;
    }

    public static List<String> prepareSemanticChunks(String text, int limit) {
        List<String> chunks = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(text);

        StringBuilder currentChunk = new StringBuilder();
        int start = iterator.first();

        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end);

            if (currentChunk.length() + sentence.length() > limit && !currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder(getContext(currentChunk));
            }
            currentChunk.append(sentence);
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }
        return chunks;
    }

    private static String getContext(StringBuilder currentChunk) {
        int lastDotIndex = currentChunk.lastIndexOf(SENTENCE_DOT_SPACE);
        return (lastDotIndex != -1) ? currentChunk.substring(lastDotIndex + DOT_OFFSET) : "";
    }
    public static String joinChunkContents(List<ChunkSearchResult> results, String separator) {
        return results.stream()
                .map(ChunkSearchResult::getContent)
                .collect(Collectors.joining(separator));
    }
}
