package eu.darkbot.ter.dks.types.plugin;

import com.google.gson.Gson;
import eu.darkbot.ter.dks.tasks.GameLogViewer;
import java.util.ArrayList;
import java.util.List;

public class GameLogViewerDTO {
    private List<String> lastStdLogs;
    private final transient GameLogViewer liveLogs;

    public GameLogViewerDTO(final GameLogViewer liveLogs) {
        this.liveLogs = liveLogs;
        this.refresh();
    }

    public void refresh() {
        int limit = 50;
        List<String> stdLines = this.liveLogs.getGameLogs(true);
        if (stdLines.size() != 0) {
            this.lastStdLogs = stdLines.subList(0, Math.min(limit, stdLines.size() - 1));
        } else {
            this.lastStdLogs = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
