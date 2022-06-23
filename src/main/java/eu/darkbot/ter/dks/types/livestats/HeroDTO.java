package eu.darkbot.ter.dks.types.livestats;

import com.google.gson.Gson;
import eu.darkbot.api.managers.HeroAPI;

public class HeroDTO {
    private Double x;
    private Double y;
    private String configuration;
    private Integer hp;
    private Integer maxHp;
    private Integer shield;
    private Integer maxShield;
    private Double hpPercent;
    private Double shieldPercent;
    private String username;
    private Boolean hasTarget;
    private Double targetX;
    private Double targetY;
    private String targetName;

    public HeroDTO(HeroAPI hero) {
        this.x = hero.getX();
        this.y = hero.getY();
        this.configuration = hero.getConfiguration().name();
        this.hp = hero.getHealth().getHp();
        this.maxHp = hero.getHealth().getMaxHp();
        this.hpPercent = hero.getHealth().hpPercent();
        this.shield = hero.getHealth().getShield();
        this.maxShield = hero.getHealth().getMaxShield();
        this.shieldPercent = hero.getHealth().shieldPercent();
        this.username = hero.getEntityInfo().getUsername();
        this.hasTarget = hero.getTarget() != null;
        if (this.hasTarget) {
            this.targetX = hero.getTarget().getX();
            this.targetY = hero.getTarget().getY();
        }
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
