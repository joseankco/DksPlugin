package eu.darkbot.ter.dks.types.livestats;

import com.google.gson.Gson;
import eu.darkbot.api.managers.StatsAPI;

import java.time.Duration;

public class StatsDTO {
    private String runningTime = "00:00:00";
    private Double totalUridium;
    private Double earnedUridium;
    private Double totalCredits;
    private Double earnedCredits;
    private Double totalHonor;
    private Double earnedHonor;
    private Double totalExperience;
    private Double earnedExperience;
    private int cargo;
    private int maxCargo;
    private int level;
    private int ping;
    private long runningTimeSeconds;

    public StatsDTO(StatsAPI stats) {
        Duration rt = stats.getRunningTime();
        this.runningTime = new StringBuilder()
                .append(String.format("%1$2s", rt.toHours()).replace(' ', '0'))
                .append(":")
                .append(String.format("%1$2s", rt.toMinutes() % 60).replace(' ', '0'))
                .append(":")
                .append(String.format("%1$2s", rt.getSeconds() % 60).replace(' ', '0'))
                .toString();
        this.runningTimeSeconds = rt.getSeconds();
        this.totalUridium = stats.getTotalUridium();
        this.earnedUridium = stats.getEarnedUridium();
        this.totalCredits = stats.getTotalCredits();
        this.earnedCredits = stats.getEarnedCredits();
        this.cargo = stats.getCargo();
        this.maxCargo = stats.getMaxCargo();
        this.totalHonor = stats.getTotalHonor();
        this.earnedHonor = stats.getEarnedHonor();
        this.totalExperience = stats.getTotalExperience();
        this.earnedExperience = stats.getEarnedExperience();
        this.level = stats.getLevel();
        this.ping = stats.getPing();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
