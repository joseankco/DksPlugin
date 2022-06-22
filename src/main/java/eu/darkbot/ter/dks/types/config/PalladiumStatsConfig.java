package eu.darkbot.ter.dks.types.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Number;

@Configuration("simple_stats")
public class PalladiumStatsConfig implements SimpleStatsConfig {

    public boolean STOP;

    @Number(min = 5, max = 60, step = 5)
    public int REFRESH_RATE_S;

    public PalladiumStatsConfig() {
        this.REFRESH_RATE_S = 5;
        this.STOP = false;
    }

    @Override
    public boolean getStop() {
        return this.STOP;
    }

    @Override
    public int getRefreshRateSec() {
        return this.REFRESH_RATE_S;
    }
}