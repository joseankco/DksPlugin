package eu.darkbot.ter.dks.types.plugin;

import com.google.gson.Gson;
import eu.darkbot.ter.dks.tasks.LogScrapper;
import eu.darkbot.ter.dks.utils.Formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LogScrapperDTO {
    private List<PatternDTO> patterns;
    private final transient LogScrapper logScrapper;

    public LogScrapperDTO(final LogScrapper logScrapper) {
        this.logScrapper = logScrapper;
        this.patterns = new ArrayList<>();
    }

    public void refresh() {
        List<String> patterns = logScrapper.getPatterns();
        HashMap<String, int[]> map = logScrapper.getPatternsMap();
        if (patterns.size() > 0) {
            this.patterns.clear();
            for (String pattern : patterns) {
                if (pattern != null) {
                    int[] info = map.get(pattern);
                    if (info != null) {
                        int occurrences = info[0], total = info[1];
                        double occurrencesHour = (occurrences / ((double) logScrapper.getRunningTime().getSeconds())) * 3600;
                        double totalHour = (total / ((double) logScrapper.getRunningTime().getSeconds())) * 3600;
                        boolean isNumbered = logScrapper.getConfig().isNumberedPattern(pattern);

                        this.patterns.add(new PatternDTO(
                                pattern,
                                Formatter.formatDoubleDots(occurrences),
                                Formatter.formatDoubleDots(occurrencesHour),
                                isNumbered ? Formatter.formatDoubleDots(total) : Formatter.formatDoubleDots(occurrences),
                                isNumbered ? Formatter.formatDoubleDots(totalHour) : Formatter.formatDoubleDots(occurrencesHour)
                        ));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(this.patterns);
    }

    private static class PatternDTO {
        private String pattern;
        private String occurrences;
        private String occurrencesh;
        private String total;
        private String totalh;

        public PatternDTO(String pattern, String occurrences, String occurrencesh, String total, String totalh) {
            this.pattern = pattern;
            this.occurrences = occurrences;
            this.occurrencesh = occurrencesh;
            this.total = total;
            this.totalh = totalh;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
}
