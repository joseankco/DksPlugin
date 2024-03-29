package eu.darkbot.ter.dks.types.remotestats;

import com.google.gson.Gson;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.game.other.EntityInfo;
import eu.darkbot.api.game.other.Health;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.managers.BoosterAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.PetAPI;
import eu.darkbot.ter.dks.utils.Formatter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private List<BoosterDTO> boosters;
    private DestinationDTO destination;
    private PetDTO pet;

    public HeroDTO(HeroAPI hero, BoosterAPI booster, PetAPI pet) {
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
        this.boosters = new ArrayList<>();
        booster.getBoosters().forEach(b -> this.boosters.add(new BoosterDTO(b)));
        this.destination = new DestinationDTO(hero.getDestination());
        this.pet = new PetDTO(pet);
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

    public static class BoosterDTO {
        private double amount;
        private String name;
        private String small;
        private String color;
        private String category;
        private double remainingTime;

        public BoosterDTO(BoosterAPI.Booster booster) {
            this.amount = booster.getAmount();
            this.category = booster.getCategory();
            Color color = booster.getColor();
            this.color = String.format("rgba(%s, %s, %s, %s)", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            this.name = booster.getName();
            this.small = booster.getSmall();
            double rmtm = booster.getRemainingTime();
            if (Double.isInfinite(rmtm)) {
                this.remainingTime = -1;
            } else if (Double.isNaN(rmtm)) {
                this.remainingTime = 0;
            } else {
                this.remainingTime = booster.getRemainingTime();
            }
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }

    public static class DestinationDTO {
        private boolean hasDestination;
        private double x;
        private double y;

        public DestinationDTO(Optional<Location> optionalLocation) {
            this.hasDestination = optionalLocation.isPresent();
            optionalLocation.ifPresent(location -> {
                this.x = location.getX();
                this.y = location.getY();
            });
        }
    }

    public static class PetDTO {
        private boolean isEnabled;
        private boolean isActive;
        private double x;
        private double y;
        private Integer hp;
        private Integer maxHp;
        private Integer shield;
        private Integer maxShield;
        private Double hpPercent;
        private Double shieldPercent;
        private double fuel;
        private double maxFuel;
        private Double fuelPercent;

        public PetDTO(PetAPI pet) {
            this.isEnabled = pet.isEnabled();
            this.isActive = pet.isActive();
            Health h = pet.getHealth();
            this.hp = h.getHp();
            this.maxHp = h.getMaxHp();
            this.shield = h.getShield();
            this.maxShield = h.getMaxShield();
            this.hpPercent = h.hpPercent();
            this.shieldPercent = h.shieldPercent();
            Location l = pet.getLocationInfo();
            this.x = l.getX();
            this.y = l.getY();
            PetAPI.PetStat ps = pet.getStat(PetAPI.Stat.FUEL);
            this.fuel = ps == null ? 0D : ps.getCurrent();
            this.maxFuel = ps == null ? 0D : ps.getTotal();
            this.fuelPercent = this.fuel / this.maxFuel;
        }
    }
}
