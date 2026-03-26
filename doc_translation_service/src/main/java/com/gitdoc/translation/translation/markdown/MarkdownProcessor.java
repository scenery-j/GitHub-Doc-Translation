package com.gitdoc.translation.translation.markdown;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts code blocks and front matter before translation,
 * restores them after AI returns the translated text.
 */
@Component
public class MarkdownProcessor {

    private static final Pattern FRONT_MATTER_PATTERN =
            Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);
    private static final Pattern FENCED_CODE_PATTERN =
            Pattern.compile("```[\\s\\S]*?```", Pattern.DOTALL);
    private static final Pattern INLINE_CODE_PATTERN =
            Pattern.compile("`[^`\\n]+`");

    public ProcessedContent preProcess(String content) {
        ProcessedContent result = new ProcessedContent();
        List<String> placeholders = new ArrayList<>();
        String text = content;

        // Extract front matter
        Matcher fmMatcher = FRONT_MATTER_PATTERN.matcher(text);
        if (fmMatcher.find()) {
            result.setFrontMatter(fmMatcher.group(0));
            text = text.substring(fmMatcher.end());
        }

        // Replace fenced code blocks with placeholders
        Matcher codeMatcher = FENCED_CODE_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        int index = 0;
        while (codeMatcher.find()) {
            String placeholder = "%%CODEBLOCK_" + index + "%%";
            placeholders.add(codeMatcher.group(0));
            codeMatcher.appendReplacement(sb, Matcher.quoteReplacement(placeholder));
            index++;
        }
        codeMatcher.appendTail(sb);
        text = sb.toString();

        // Replace inline code with placeholders
        Matcher inlineMatcher = INLINE_CODE_PATTERN.matcher(text);
        sb = new StringBuffer();
        while (inlineMatcher.find()) {
            String placeholder = "%%INLINE_" + index + "%%";
            placeholders.add(inlineMatcher.group(0));
            inlineMatcher.appendReplacement(sb, Matcher.quoteReplacement(placeholder));
            index++;
        }
        inlineMatcher.appendTail(sb);
        text = sb.toString();

        result.setProcessedContent(text);
        result.setPlaceholders(placeholders);
        return result;
    }

    public String postProcess(String translatedContent, ProcessedContent original) {
        String text = translatedContent;

        // Restore placeholders in reverse order to handle nested
        for (int i = original.getPlaceholders().size() - 1; i >= 0; i--) {
            String key;
            if (i < countFencedBlocks(original.getProcessedContent())) {
                key = "%%CODEBLOCK_" + i + "%%";
            } else {
                key = "%%INLINE_" + i + "%%";
            }
            text = text.replace(key, original.getPlaceholders().get(i));
        }

        // Prepend front matter
        if (original.getFrontMatter() != null) {
            text = original.getFrontMatter() + text;
        }

        return text;
    }

    private int countFencedBlocks(String content) {
        Matcher m = FENCED_CODE_PATTERN.matcher(content);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    // ── Section-level splitting for incremental translation ──────────────────

    /**
     * Splits a Markdown document into sections based on level-1 and level-2 headings.
     *
     * <p>Splitting rule:
     * <ul>
     *   <li>A new section begins at every line starting with {@code #} or {@code ##}
     *       that is NOT inside a fenced code block.</li>
     *   <li>If the document has no such headings the entire content is returned as a
     *       single section (incremental translation degrades to full translation).</li>
     * </ul>
     *
     * @return ordered list of section strings, each including its heading line.
     * Never empty — at minimum one section contains the whole document.
     */
    public List<String> splitIntoSections(String content) {
        // First, mask fenced code blocks so heading detection ignores them
        String masked = FENCED_CODE_PATTERN.matcher(content).replaceAll(mr -> {
            // Replace each code block with the same number of newlines to keep line positions stable
            String block = mr.group();
            long lines = block.chars().filter(c -> c == '\n').count();
            return "\n".repeat((int) lines);
        });

        String[] lines = content.split("\n", -1);
        String[] maskedLines = masked.split("\n", -1);

        List<Integer> breakPoints = new ArrayList<>();
        breakPoints.add(0);

        for (int i = 0; i < maskedLines.length; i++) {
            String ml = maskedLines[i];
            // Match # or ## at start of line (not ###, ####, etc.)
            if (ml.matches("^#{1,2}\\s+.*") && i > 0) {
                breakPoints.add(i);
            }
        }

        if (breakPoints.size() == 1) {
            // No headings found — whole document is one section
            return List.of(content);
        }

        List<String> sections = new ArrayList<>();
        for (int s = 0; s < breakPoints.size(); s++) {
            int start = breakPoints.get(s);
            int end = (s + 1 < breakPoints.size()) ? breakPoints.get(s + 1) : lines.length;
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                if (i > start) sb.append('\n');
                sb.append(lines[i]);
            }
            sections.add(sb.toString());
        }
        return sections;
    }

    /**
     * Joins a list of sections back into a single document.
     * Adjacent sections are separated by a single newline (the sections already
     * end without a trailing newline from {@link #splitIntoSections}).
     */
    public String joinSections(List<String> sections) {
        return String.join("\n", sections);
    }
}
