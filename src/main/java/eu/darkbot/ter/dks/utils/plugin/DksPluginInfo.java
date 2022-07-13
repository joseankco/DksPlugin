package eu.darkbot.ter.dks.utils.plugin;

import com.google.gson.Gson;
import eu.darkbot.ter.dks.types.plugin.GameLogViewerDTO;
import eu.darkbot.ter.dks.types.plugin.GameLogScrapperDTO;
import eu.darkbot.ter.dks.types.plugin.PalladiumStatsDTO;

public class DksPluginInfo {
    private GameLogViewerDTO liveLogs;
    private GameLogScrapperDTO logScrapper;
    private PalladiumStatsDTO palladiumStats;

    public DksPluginInfo() {
    }

    public void setGameLogViewerDTO(GameLogViewerDTO dto) {
        this.liveLogs = dto;
    }

    public GameLogViewerDTO getGameLogViewer() {
        return this.liveLogs;
    }

    public void setGameLogScrapper(GameLogScrapperDTO dto) {
        this.logScrapper = dto;
    }

    public GameLogScrapperDTO getGameLogScrapper() {
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
