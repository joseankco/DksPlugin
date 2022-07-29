package eu.darkbot.ter.dks.types.remotestats;

import eu.darkbot.api.game.entities.Barrier;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import java.util.ArrayList;

public class MapDTO {
    private Double boundX;
    private Double boundY;
    private Integer id;
    private String name;
    private String mapID;
    private ArrayList<PortalDTO> portals;
    private ArrayList<NpcDTO> npcs;
    private ArrayList<PlayerDTO> players;
    private ArrayList<BarrierDTO> barriers;

    public MapDTO(StarSystemAPI map, EntitiesAPI entities) {
        GameMap current = map.getCurrentMap();
        this.id = current.getId();
        this.name = current.getName();
        this.mapID = String.valueOf(current.getId());
        this.boundX = map.getCurrentMapBounds().getX2();
        this.boundY = map.getCurrentMapBounds().getY2();
        portals = new ArrayList<>();
        npcs = new ArrayList<>();
        players = new ArrayList<>();
        barriers = new ArrayList<>();
        new ArrayList<>(entities.getPortals()).forEach(portal -> portals.add(new PortalDTO(portal)));
        new ArrayList<>(entities.getNpcs()).forEach(npc -> npcs.add(new NpcDTO(npc)));
        new ArrayList<>(entities.getPlayers()).forEach(player -> players.add(new PlayerDTO(player)));
        new ArrayList<>(entities.getBarriers()).forEach(barrier -> barriers.add(new BarrierDTO(barrier)));
    }

    public static class NpcDTO {
        private Double x;
        private Double y;
        private String name;
        public NpcDTO(Npc npc) {
            this.x = npc.getX();
            this.y = npc.getY();
            this.name = npc.getEntityInfo().getUsername();
        }
    }

    public static class PlayerDTO {
        private Double x;
        private Double y;
        private String name;
        private boolean isEnemy;
        public PlayerDTO(Player player) {
            this.x = player.getX();
            this.y = player.getY();
            this.name = player.getEntityInfo().getUsername();
            this.isEnemy = player.getEntityInfo().isEnemy();
        }
    }

    public static class PortalDTO {
        private Double x;
        private Double y;
        public PortalDTO(Portal portal) {
            this.x = portal.getX();
            this.y = portal.getY();
        }
    }

    public static class BarrierDTO {
        private double x1;
        private double x2;
        private double y1;
        private double y2;

        public BarrierDTO(Barrier barrier) {
            Area.Rectangle r = barrier.getZoneArea().getBounds();
            this.x1 = r.getX();
            this.y1 = r.getY();
            this.x2 = r.getX2();
            this.y2 = r.getY2();
        }
    }
}
