package eu.darkbot.ter.dks.types.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Number;

// DarkBot Imports
import com.github.manolo8.darkbot.gui.tree.components.JFileOpener;
import com.github.manolo8.darkbot.config.types.Editor;

@Configuration("remote_stats")
public class RemoteStatsConfig {

    public boolean ACTIVE = false;

    public String HOST = "localhost";

    @Number(min = 2000, max = 9999, step = 1)
    public int PORT = 8085;

    @Number(min = 1, max = 3600, step = 1)
    public int REFRESH_RATE_S = 0;

    public String NGROK_AUTH = "";

    @Editor(JFileOpener.class)
    public String SERVER_FILE = "";

    public void setServerFile(String path) {
        this.SERVER_FILE = path;
    }
}
