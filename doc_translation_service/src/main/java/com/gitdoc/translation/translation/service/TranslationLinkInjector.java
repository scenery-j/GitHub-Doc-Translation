package com.gitdoc.translation.translation.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Injects a multi-language translation links block at the top of a Markdown source file.
 * The block is idempotent: running multiple times replaces the existing block rather than appending.
 */
@Component
public class TranslationLinkInjector {

    static final String START_MARKER = "<!--TRANSLATION_LINKS_START-->";
    static final String END_MARKER = "<!--TRANSLATION_LINKS_END-->";

    private static final Map<String, String> LANG_DISPLAY_NAMES = new LinkedHashMap<>();

    static {
        LANG_DISPLAY_NAMES.put("ar", "العربية");
        LANG_DISPLAY_NAMES.put("bg", "Български");
        LANG_DISPLAY_NAMES.put("bn", "বাংলা");
        LANG_DISPLAY_NAMES.put("cs", "Čeština");
        LANG_DISPLAY_NAMES.put("da", "Dansk");
        LANG_DISPLAY_NAMES.put("de", "Deutsch");
        LANG_DISPLAY_NAMES.put("el", "Ελληνικά");
        LANG_DISPLAY_NAMES.put("en", "English");
        LANG_DISPLAY_NAMES.put("es", "Español");
        LANG_DISPLAY_NAMES.put("fi", "Suomi");
        LANG_DISPLAY_NAMES.put("fr", "Français");
        LANG_DISPLAY_NAMES.put("he", "עברית");
        LANG_DISPLAY_NAMES.put("hi", "हिन्दी");
        LANG_DISPLAY_NAMES.put("hu", "Magyar");
        LANG_DISPLAY_NAMES.put("id", "Indonesia");
        LANG_DISPLAY_NAMES.put("it", "Italiano");
        LANG_DISPLAY_NAMES.put("ja", "日本語");
        LANG_DISPLAY_NAMES.put("ko", "한국어");
        LANG_DISPLAY_NAMES.put("ms", "Bahasa Melayu");
        LANG_DISPLAY_NAMES.put("nl", "Nederlands");
        LANG_DISPLAY_NAMES.put("no", "Norsk");
        LANG_DISPLAY_NAMES.put("pl", "Polski");
        LANG_DISPLAY_NAMES.put("pt", "Português");
        LANG_DISPLAY_NAMES.put("pt-BR", "Português (Brasil)");
        LANG_DISPLAY_NAMES.put("ro", "Română");
        LANG_DISPLAY_NAMES.put("ru", "Русский");
        LANG_DISPLAY_NAMES.put("sr", "Српски");
        LANG_DISPLAY_NAMES.put("sv", "Svenska");
        LANG_DISPLAY_NAMES.put("th", "ภาษาไทย");
        LANG_DISPLAY_NAMES.put("tr", "Türkçe");
        LANG_DISPLAY_NAMES.put("uk", "Українська");
        LANG_DISPLAY_NAMES.put("ur", "اردو");
        LANG_DISPLAY_NAMES.put("vi", "Tiếng Việt");
        LANG_DISPLAY_NAMES.put("zh", "中文 (简体)");
        LANG_DISPLAY_NAMES.put("zh-CN", "中文 (简体)");
        LANG_DISPLAY_NAMES.put("zh-TW", "中文 (繁體)");
    }

    /**
     * Injects a translation links block at the top of {@code sourceContent}.
     * If a block already exists, it is replaced entirely (idempotent).
     *
     * @param sourceContent    current raw content of the source Markdown file
     * @param sourcePath       repo-relative path of the source file (e.g. {@code README.md})
     * @param langToTargetPath map of language code → repo-relative target path
     * @return modified content, or the original content unchanged if there are no translations
     */
    public String injectLinks(String sourceContent, String sourcePath,
                              Map<String, String> langToTargetPath) {
        if (langToTargetPath.isEmpty()) {
            return sourceContent;
        }
        String block = buildBlock(sourcePath, langToTargetPath);
        String cleanContent = removeExistingBlock(sourceContent).stripLeading();
        return block + "\n\n" + cleanContent;
    }

    private String buildBlock(String sourcePath, Map<String, String> langToTargetPath) {
        StringBuilder links = new StringBuilder();
        langToTargetPath.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String lang = e.getKey();
                    String displayName = LANG_DISPLAY_NAMES.getOrDefault(lang, lang);
                    String relPath = computeRelativePath(sourcePath, e.getValue());
                    if (!links.isEmpty()) links.append(" | ");
                    links.append("[").append(displayName)
                            .append(" (").append(lang).append(")](").append(relPath).append(")");
                });

        return START_MARKER + "\n" +
                "#### Supported by [GitHub Doc Translation](https://github.com/scenery-j/GitHub-Doc-Translation)" + "\n" +
                "> 📖 **其他语言版本**：" + links + "\n" +
                END_MARKER;
    }

    /**
     * Removes an existing translation links block from the content (if present).
     */
    String removeExistingBlock(String content) {
        int start = content.indexOf(START_MARKER);
        if (start < 0) return content;
        int end = content.indexOf(END_MARKER, start);
        if (end < 0) return content;
        end += END_MARKER.length();
        // Consume trailing newlines that were appended after the block
        while (end < content.length() && content.charAt(end) == '\n') end++;
        return content.substring(0, start) + content.substring(end);
    }

    /**
     * Computes a relative path from {@code sourcePath}'s directory to {@code targetPath}.
     * GitHub renders relative links correctly in Markdown.
     * e.g. source=docs/README.md, target=translations/en/docs/README.md → ../translations/en/docs/README.md
     */
    String computeRelativePath(String sourcePath, String targetPath) {
        String sourceDir = sourcePath.contains("/")
                ? sourcePath.substring(0, sourcePath.lastIndexOf('/'))
                : "";
        if (sourceDir.isEmpty()) {
            return targetPath;
        }
        int levels = sourceDir.split("/").length;
        return "../".repeat(levels) + targetPath;
    }
}
