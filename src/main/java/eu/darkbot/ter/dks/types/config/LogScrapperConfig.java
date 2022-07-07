package eu.darkbot.ter.dks.types.config;

import eu.darkbot.api.config.annotations.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Configuration("log_scrapper")
public class LogScrapperConfig {

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
        int idx = -1;
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            if (pattern.equals(toDeletePattern)) {
                idx = i;
            }
        }
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
}
