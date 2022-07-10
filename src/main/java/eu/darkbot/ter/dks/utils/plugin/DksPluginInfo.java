package eu.darkbot.ter.dks.utils.plugin;

import com.google.gson.Gson;
import eu.darkbot.ter.dks.tasks.LiveLogs;
import eu.darkbot.ter.dks.types.plugin.LiveLogsDTO;
import eu.darkbot.ter.dks.types.plugin.LogScrapperDTO;
import eu.darkbot.ter.dks.types.plugin.PalladiumStatsDTO;

import java.util.ArrayList;

public class DksPluginInfo {
    private LiveLogsDTO liveLogs;
    private LogScrapperDTO logScrapper;
    private PalladiumStatsDTO palladiumStats;

    public DksPluginInfo() {
    }

    public void setLiveLogs(LiveLogsDTO dto) {
        this.liveLogs = dto;
    }

    public LiveLogsDTO getLiveLogs() {
        return this.liveLogs;
    }

    public void setLogScrapper(LogScrapperDTO dto) {
        this.logScrapper = dto;
    }

    public LogScrapperDTO getLogScrapper() {
        return this.logScrapper;
    }

    public void setPalladiumStats(PalladiumStatsDTO dto) {
        this.palladiumStats = dto;
    }

    public PalladiumStatsDTO getPalladiumStats() {
        return this.palladiumStats;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
