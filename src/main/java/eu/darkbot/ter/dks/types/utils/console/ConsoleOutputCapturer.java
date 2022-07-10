package eu.darkbot.ter.dks.types.utils.console;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleOutputCapturer {

    private ByteArrayOutputStream baosStd;
    private ByteArrayOutputStream baosErr;
    private boolean capturing;

    private List<String> lines_std;
    private List<String> lines_std_dates;
    private final List<String> lines_std_new;
    private Integer MAX_LINES_STD;

    private List<String> lines_err;
    private List<String> lines_err_dates;
    private final List<String> lines_err_new;
    private Integer MAX_LINES_ERR;

    private boolean isTimed = true;

    public ConsoleOutputCapturer() {
        this.lines_std = new ArrayList<>();
        this.lines_std_dates = new ArrayList<>();
        this.lines_std_new = new ArrayList<>();
        this.lines_err = new ArrayList<>();
        this.lines_err_dates = new ArrayList<>();
        this.lines_err_new = new ArrayList<>();
    }

    public void setMaxLinesStd(Integer max) {
        this.MAX_LINES_STD = max;
    }

    public void setMaxLinesErr(Integer max) {
        this.MAX_LINES_ERR = max;
    }

    public void setTimed(boolean timed) {
        this.isTimed = timed;
    }

    public void setCustom(PrintStream std, PrintStream out) {
        System.setOut(std);
        System.setErr(out);
    }

    public void setPrevious() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    public void start() {
        if (!this.isCapturing()) {
            capturing = true;
            baosStd = new ByteArrayOutputStream();
            baosErr = new ByteArrayOutputStream();
            OutputStream outputStreamCombinerStd = new OutputStreamCombiner(Arrays.asList(System.out, baosStd));
            PrintStream customStd = new PrintStream(outputStreamCombinerStd);
            OutputStream outputStreamCombinerErr = new OutputStreamCombiner(Arrays.asList(System.err, baosErr));
            PrintStream customErr = new PrintStream(outputStreamCombinerErr);
            this.setCustom(customStd, customErr);
        }
    }

    public void refresh() {
        this.stop();
        this.start();
    }

    public void reset() {
        this.baosStd.reset();
        this.lines_std = new ArrayList<>();
        this.lines_std_dates = new ArrayList<>();

        this.baosErr.reset();
        this.lines_err = new ArrayList<>();
        this.lines_err_dates = new ArrayList<>();
    }

    public void stop() {
        if (this.isCapturing()) {
            this.setPrevious();
            baosStd = null;
            baosErr = null;
            capturing = false;
        }
    }

    public boolean isCapturing() {
        return this.capturing;
    }

    public void updateLines() {
        this.updateLinesBaos(this.baosStd, this.lines_std, this.lines_std_dates, this.lines_std_new, this.MAX_LINES_STD);
        this.updateLinesBaos(this.baosErr, this.lines_err, this.lines_err_dates, this.lines_err_new, this.MAX_LINES_ERR);
    }

    private void updateLinesBaos(ByteArrayOutputStream baos, List<String> lines, List<String> lines_dates, List<String> lines_new, Integer max_lines) {
        if (baos != null) {
            SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
            String date = format.format(new Date());

            List<String> newlines = Arrays.asList(baos.toString().split("\\r?\\n"));
            lines_new.clear();
            if (newlines.size() > 0) {
                for (String newline: newlines) {
                    if (!newline.equals("")) {
                        lines.add(newline);
                        lines_dates.add(date);
                        lines_new.add(newline);
                    }
                }
                baos.reset();
            }
            if (max_lines != null && lines.size() > max_lines) {
                int diff = lines.size() - max_lines;
                for (int i = diff; i >= 0; i--) {
                    lines.remove(i);
                    lines_dates.remove(i);
                }
            }
        }
    }

    public List<String> getNewLinesStd() {
        return this.lines_std_new;
    }

    public List<String> getNewLinesErr() {
        return this.lines_std_new;
    }

    private List<String> getLinesBaos(List<String> lines, boolean reversed) {
        if (reversed) {
            List<String> copy = new ArrayList<>(lines);
            Collections.reverse(copy);
            return copy;
        }
        return lines;
    }

    private String getLinesStringBaos(List<String> linesBaos, List<String> lines_dates, boolean reversed, String filter) {
        List<String> lines = this.getLinesBaos(linesBaos, reversed);
        StringBuilder sb = new StringBuilder();
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            int idx = reversed ? (size - 1) - i : i;
            String line = linesBaos.get(idx);

            boolean matches = true;
            if (filter != null) {
                try {
                    Pattern pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(line);
                    matches = matcher.find();
                } catch (Exception ignored) {}
            }

            if (matches) {
                if (this.isTimed) {
                    sb.append(lines_dates.get(idx)).append(" ");
                }
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public List<String> getLinesStd(boolean reversed) {
        return this.getLinesBaos(this.lines_std, reversed);
    }

    public String getLinesStringStd(boolean reversed, String filter) {
        return this.getLinesStringBaos(this.lines_std, this.lines_std_dates, reversed, filter);
    }

    public List<String> getLinesErr(boolean reversed) {
        return this.getLinesBaos(this.lines_err, reversed);
    }

    public String getLinesStringErr(boolean reversed) {
        return this.getLinesStringBaos(this.lines_err, this.lines_err_dates, reversed, null);
    }
}