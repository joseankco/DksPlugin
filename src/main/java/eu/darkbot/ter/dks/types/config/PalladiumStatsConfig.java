package eu.darkbot.ter.dks.types.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Number;

@Configuration("simple_stats")
public class PalladiumStatsConfig implements SimpleStatsConfig {

    public boolean STOP;

    @Number(min = 1, max = 60, step = 1)
    public int REFRESH_RATE_S;

    public boolean SHOULD_IGNORE_BOT_STOPPED;

    public PalladiumStatsConfig() {
        this.REFRESH_RATE_S = 1;
        this.STOP = false;
        this.SHOULD_IGNORE_BOT_STOPPED = false;
    }

    @Override
    public boolean getStop() {
        return this.STOP;
    }

    @Override
    public int getRefreshRateSec() {
        return this.REFRESH_RATE_S;
    }

    @Override
    public boolean getShouldIgnoreBotStopped() {
        return this.SHOULD_IGNORE_BOT_STOPPED;
    }
}