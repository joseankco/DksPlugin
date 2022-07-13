package eu.darkbot.ter.dks.types.plugin;

import eu.darkbot.ter.dks.behaviours.PalladiumStats;

public class PalladiumStatsDTO {
    private String status;
    private String runningTime;
    private String total;
    private String totalh;
    private String eeh;
    private final transient PalladiumStats palladiumStats;

    public PalladiumStatsDTO(final PalladiumStats palladiumStats) {
        this.palladiumStats = palladiumStats;
    }

    private void setData(String status, String runningTime, String total, String totalh, String eeh) {
        this.status = status;
        this.runningTime = runningTime;
        this.total = total;
        this.totalh = totalh;
        this.eeh = eeh;
    }

    public void refresh() {
        this.setData(
                this.palladiumStats.getStatus(),
                this.palladiumStats.getRunningTime(),
                this.palladiumStats.getCollected(),
                this.palladiumStats.getCollectedHour(),
                this.palladiumStats.getExtraEnergyHour()
        );
    }
}
