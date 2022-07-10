package eu.darkbot.ter.dks.types.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import eu.darkbot.ter.dks.tasks.LiveLogs;
import java.util.ArrayList;
import java.util.List;

public class LiveLogsDTO {
    private List<String> lastStdLogs;
    private List<String> lastErrLogs;
    private final transient LiveLogs liveLogs;

    public LiveLogsDTO(final LiveLogs liveLogs) {
        this.liveLogs = liveLogs;
        this.refresh();
    }

    public void refresh() {
        int limit = 50;
        List<String> stdLines = this.liveLogs.getCapturer().getLinesStd(true);
        if (stdLines.size() != 0) {
            this.lastStdLogs = stdLines.subList(0, Math.min(limit, stdLines.size() - 1));
        } else {
            this.lastStdLogs = new ArrayList<>();
        }
        List<String> errLines = this.liveLogs.getCapturer().getLinesErr(false);
        if (errLines.size() != 0) {
            this.lastErrLogs = errLines.subList(0, Math.min(limit, errLines.size() - 1));
        } else {
            this.lastErrLogs = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
