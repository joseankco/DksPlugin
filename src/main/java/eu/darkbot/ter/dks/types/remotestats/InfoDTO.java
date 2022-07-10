package eu.darkbot.ter.dks.types.remotestats;

import com.google.gson.Gson;
import eu.darkbot.ter.dks.utils.plugin.DksPluginInfo;

public class InfoDTO {
    private HeroDTO hero;
    private StatsDTO stats;
    private ModuleDTO module;
    private MapDTO map;
    private DksPluginInfo plugin;

    public InfoDTO(HeroDTO hero, StatsDTO stats, ModuleDTO module, MapDTO map, DksPluginInfo plugin) {
        this.hero = hero;
        this.stats = stats;
        this.module = module;
        this.map = map;
        this.plugin = plugin;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
