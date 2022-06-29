package eu.darkbot.ter.dks.tasks;

import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Task;
import eu.darkbot.api.managers.*;
import eu.darkbot.ter.dks.types.VerifierChecker;
import eu.darkbot.ter.dks.types.config.RemoteStatsConfig;
import eu.darkbot.ter.dks.types.livestats.*;
import eu.darkbot.ter.dks.types.utils.Http;

import java.util.Arrays;

@Feature(name = "Remote Stats", description = "Sends hero stats to a Server")
public class RemoteStats implements Task, Configurable<RemoteStatsConfig> {

    protected final HeroAPI heroAPI;
    protected final StarSystemAPI mapAPI;
    protected final BotAPI botAPI;
    protected final StatsAPI statsAPI;
    protected final EntitiesAPI entitiesAPI;

    private RemoteStatsConfig config;

    private long nextTick = 0;

    public RemoteStats(
            AuthAPI auth,
            HeroAPI hero,
            StarSystemAPI map,
            BotAPI bot,
            StatsAPI stats,
            EntitiesAPI entities
    ) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        this.heroAPI = hero;
        this.mapAPI = map;
        this.botAPI = bot;
        this.statsAPI = stats;
        this.entitiesAPI = entities;
    }

    public String getURL() {
        String url = new StringBuilder("http://")
                .append(this.config.HOST)
                .append(":")
                .append(this.config.PORT)
                .toString();
        return url;
    }

    public String getMessage() {
        return new InfoDTO(
                new HeroDTO(this.heroAPI),
                new StatsDTO(this.statsAPI),
                new ModuleDTO(this.botAPI),
                new MapDTO(this.mapAPI, this.entitiesAPI)
        ).toJson();
    }

    public boolean checkTick() {
        boolean canTick = this.config.ACTIVE && this.nextTick <= System.currentTimeMillis();
        if (canTick) {
            this.nextTick = System.currentTimeMillis() + (this.config.REFRESH_RATE_S * 1000L);
        }
        return canTick;
    }

    @Override
    public void onTickTask() {
        if (this.checkTick()) {
            String url = this.getURL();
            String message = this.getMessage();
            Http.sendMessage(message, url);
        }
    }

    @Override
    public void setConfig(ConfigSetting<RemoteStatsConfig> arg0) {
        this.config = arg0.getValue();
    }
}
