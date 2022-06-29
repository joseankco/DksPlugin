package eu.darkbot.ter.dks.types.remotestats;

import com.google.gson.Gson;

public class InfoDTO {
    private HeroDTO hero;
    private StatsDTO stats;
    private ModuleDTO module;
    private MapDTO map;

    public InfoDTO(HeroDTO hero, StatsDTO stats, ModuleDTO module, MapDTO map) {
        this.hero = hero;
        this.stats = stats;
        this.module = module;
        this.map = map;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
