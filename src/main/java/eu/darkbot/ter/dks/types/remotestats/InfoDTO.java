package eu.darkbot.ter.dks.types.remotestats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.darkbot.ter.dks.utils.NumberOnlyTypeAdapterFactory;
import eu.darkbot.ter.dks.utils.plugin.DksPluginInfo;

public class InfoDTO {
    private HeroDTO hero;
    private StatsDTO stats;
    private ModuleDTO module;
    private MapDTO map;
    private DksPluginInfo plugin;
    private UserDataDTO sesion;
    private DeathsDTO deaths;
    private ConfigDTO config;
    private long tick;

    public InfoDTO(
            HeroDTO hero,
            StatsDTO stats,
            ModuleDTO module,
            MapDTO map,
            DksPluginInfo plugin,
            UserDataDTO sesion,
            DeathsDTO deaths,
            ConfigDTO config
    ) {
        this.hero = hero;
        this.stats = stats;
        this.module = module;
        this.map = map;
        this.plugin = plugin;
        this.sesion = sesion;
        this.deaths = deaths;
        this.config = config;
        this.tick = System.currentTimeMillis();
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapterFactory(NumberOnlyTypeAdapterFactory.getInstance())
                .create();

        return gson.toJson(this);
    }
}
