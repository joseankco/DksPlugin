package eu.darkbot.ter.dks.utils;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.List;

public class Formatter {
    public static String formatDuration(Duration duration) {
        long s = Math.abs(duration.getSeconds());
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    public static String formatDoubleDots(double value) {
        DecimalFormat df = new DecimalFormat("###,###,###");
        return df.format(value).replace(",", ".");
    }

    public static String formatHtmlTag(String inner) {
        return String.format("<html>%s</html>", inner);
    }

    public static String formatBoldTag(String inner) {
        return String.format("<b>%s</b>", inner);
    }

    public static String joinList(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for(String line: list) {
            sb.append(line).append(delimiter);
        }
        return sb.toString();
    }
}
