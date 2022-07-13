package eu.darkbot.ter.dks.types.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Number;

@Configuration("game_log_viewer")
public class GameLogViewerConfig {

    @Number(min = 10, max = 10000, step = 10)
    public int MAX_STD_LINES = 100;

    public boolean TIMED = true;
}
