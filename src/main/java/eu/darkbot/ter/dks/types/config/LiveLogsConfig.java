package eu.darkbot.ter.dks.types.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Number;

@Configuration("live_logs")
public class LiveLogsConfig {

    @Number(min = 10, max = 10000, step = 10)
    public int MAX_STD_LINES = 100;

    @Number(min = 10, max = 10000, step = 10)
    public int MAX_ERR_LINES = 100;

    public boolean TIMED = true;
}
