package eu.darkbot.ter.dks.types.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Readonly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Configuration("log_scrapper")
public class LogScrapperConfig {

    @Readonly
    public String PATTERNS;

    public LogScrapperConfig() {
        this.PATTERNS = "";
    }

    public List<String> getPatterns() {
        List<String> patterns = new ArrayList<>(Arrays.asList(this.PATTERNS.split(";")));
        patterns.removeIf(p -> p.equals(""));
        return patterns;
    }

    public void removePattern(String toDeletePattern) {
        List<String> patterns = this.getPatterns();
        int idx = patterns.indexOf(toDeletePattern);
        if (idx != -1) {
            patterns.remove(idx);
        }
        this.setPatterns(patterns);
    }

    public void setPatterns(List<String> patterns) {
        this.PATTERNS = String.join(";", patterns);
    }

    public boolean hasPattern(String pattern) {
        List<String> patterns = this.getPatterns();
        return patterns.contains(pattern);
    }

    public void addPattern(String pattern) {
        if (!pattern.trim().equals("") && !this.hasPattern(pattern.trim())) {
            if (this.PATTERNS.endsWith(";")) {
                this.PATTERNS = this.PATTERNS + pattern + ";";
            } else {
                this.PATTERNS = this.PATTERNS + ";" + pattern + ";";
            }
        }
    }

    public String getSanitizedPattern(String pattern) {
        String regExNumber = "(?<ExtractedNumber>\\d+\\.?\\d*)";
        return pattern
                .replace("{n}", regExNumber)
                .replace("{N}", regExNumber)
                .replace("(i)", "")
                .replace("(I)", "")
                .trim();
    }

    public Pattern getRegExPattern(String pattern) {
        String sanitizedPattern = this.getSanitizedPattern(pattern);
        if (this.isCaseInsensitivePattern(pattern)) {
            return Pattern.compile(sanitizedPattern, Pattern.CASE_INSENSITIVE);
        } else {
            return Pattern.compile(sanitizedPattern);
        }
    }

    public boolean isNumberedPattern(String pattern) {
        return pattern.toLowerCase().contains("{n}");
    }

    public boolean isCaseInsensitivePattern(String pattern) {
        return pattern.toLowerCase().startsWith("(i)");
    }

    public void patternUp(String pattern) {
        List<String> patterns = this.getPatterns();
        int idx = patterns.indexOf(pattern);
        if (idx > 0) {
            Collections.swap(patterns, idx, idx - 1);
            this.setPatterns(patterns);
        }
    }

    public void patternDown(String pattern) {
        List<String> patterns = this.getPatterns();
        int idx = patterns.indexOf(pattern);
        if (idx < (patterns.size() - 1)) {
            Collections.swap(patterns, idx, idx + 1);
            this.setPatterns(patterns);
        }
    }

    public void editPattern(String pattern, String newPattern) {
        List<String> patterns = this.getPatterns();
        int idx = patterns.indexOf(pattern);
        patterns.set(idx, newPattern);
        this.setPatterns(patterns);
    }
}
