package eu.darkbot.ter.dks.types.remotestats;

import com.google.gson.Gson;
import eu.darkbot.api.managers.RepairAPI;

import java.time.Instant;
import java.util.Objects;

public class DeathsDTO {
    private int numDeaths;
    private long lastDeathMilliseconds;
    private String lastDestroyerName;
    private boolean isDestroyed;

    public DeathsDTO(RepairAPI rep) {
        this.numDeaths = rep.getDeathAmount();
        Instant i = rep.getLastDeathTime();
        this.lastDeathMilliseconds = i == null ? 0L : i.toEpochMilli();
        this.lastDestroyerName = rep.getLastDestroyerName();
        this.isDestroyed = rep.isDestroyed();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
