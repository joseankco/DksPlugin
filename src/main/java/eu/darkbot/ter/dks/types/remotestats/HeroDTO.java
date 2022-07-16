package eu.darkbot.ter.dks.types.remotestats;

import com.google.gson.Gson;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.game.other.EntityInfo;
import eu.darkbot.api.game.other.Health;
import eu.darkbot.api.managers.HeroAPI;

public class HeroDTO {
    private Integer id;
    private Double x;
    private Double y;
    private String configuration;
    private Integer hp;
    private Integer maxHp;
    private Integer shield;
    private Integer maxShield;
    private Integer hull;
    private Integer maxHull;
    private Double hpPercent;
    private Double shieldPercent;
    private Double hullPercent;
    private String username;
    private TargetDTO target;

    public HeroDTO(HeroAPI hero) {
        this.id = hero.getId();
        this.x = hero.getX();
        this.y = hero.getY();
        this.configuration = hero.getConfiguration().name();
        this.hp = hero.getHealth().getHp();
        this.maxHp = hero.getHealth().getMaxHp();
        this.hpPercent = hero.getHealth().hpPercent();
        this.shield = hero.getHealth().getShield();
        this.maxShield = hero.getHealth().getMaxShield();
        this.shieldPercent = hero.getHealth().shieldPercent();
        this.hull = hero.getHealth().getHull();
        this.maxHull = hero.getHealth().getMaxHull();
        this.hullPercent = hero.getHealth().hullPercent();
        this.username = hero.getEntityInfo().getUsername();
        this.target = new TargetDTO(hero.getTarget());
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static class TargetDTO {
        private Boolean isValid;
        private Boolean isEnemy;
        private Double x;
        private Double y;
        private String name;
        private Integer hp;
        private Integer maxHp;
        private Integer shield;
        private Integer maxShield;
        private Double hpPercent;
        private Double shieldPercent;

        public TargetDTO(Entity target) {
            this.isValid = target != null;
            if (this.isValid) {
                this.x = target.getX();
                this.y = target.getY();
                EntityInfo info = null;
                Health health = null;
                boolean isNpc = false;
                if (target instanceof Npc) {
                    info = ((Npc) target).getEntityInfo();
                    health = ((Npc) target).getHealth();
                    isNpc = true;
                } else if (target instanceof Ship) {
                    info = ((Ship) target).getEntityInfo();
                    health = ((Ship) target).getHealth();
                }

                this.isValid = info != null && health != null;
                if (this.isValid) {
                    this.isEnemy = isNpc || info.isEnemy();
                    this.name = info.getUsername();
                    this.hp = health.getHp();
                    this.shield = health.getShield();
                    this.maxHp = health.getMaxHp();
                    this.maxShield = health.getMaxShield();
                    this.hpPercent = health.hpPercent();
                    this.shieldPercent = health.shieldPercent();
                }
            }
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }
}
