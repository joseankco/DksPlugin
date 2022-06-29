package eu.darkbot.ter.dks.types.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Number;

@Configuration("remote_stats")
public class RemoteStatsConfig {

    public boolean ACTIVE = false;

    public String HOST = "localhost";

    @Number(min = 2000, max = 9999, step = 1)
    public int PORT = 8085;

    @Number(min = 1, max = 3600, step = 1)
    public int REFRESH_RATE_S = 0;
}
