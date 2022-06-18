package com.dks.types.config;

import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;

public interface SimpleStatsConfig {
    @Option(value = "Stop", description = "Will stop the task")
    boolean STOP = false;

    @Option(value = "Refresh Rate (s)", description = "Refresh Rate in Seconds to refresh data")
    @Num(min = 1, max = 120, step = 1)
    int REFRESH_RATE_S = 1;
}
