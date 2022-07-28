package eu.darkbot.ter.dks.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigHandler;
import com.google.gson.Gson;
import eu.darkbot.api.managers.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class BotRemoteController {
    protected ServerReponse response;
    protected long lastTick;

    protected BotAPI bot;
    protected Main main;
    protected ExtensionsAPI extensions;
    protected ConfigAPI config;
    protected StatsAPI stats;

    protected String id;
    protected String hashedId;

    public BotRemoteController(
            BotAPI bot,
            Main main,
            HeroAPI hero,
            ExtensionsAPI extensions,
            ConfigAPI config,
            StatsAPI stats
    ) {
        this.response = new ServerReponse();
        this.lastTick = 0L;
        this.bot = bot;
        this.main = main;
        this.id = String.valueOf(hero.getId());
        this.hashedId = this.getHashedId();
        this.extensions = extensions;
        this.config = config;
        this.stats = stats;
    }

    private String getHashedId() {
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(this.id.getBytes(StandardCharsets.UTF_8));
            BigInteger bi = new BigInteger(1, bytes);
            return String.format("%0" + (bytes.length << 1) + "x", bi);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return this.id;
        }
    }

    public boolean setServerResponse(String json) {
        ServerReponse aux = ServerReponse.fromJson(json);
        if (this.checkIfForMe(aux) && this.checkIfDifferent(aux)) {
            this.response = aux;
            this.lastTick = this.response.tick;
            return true;
        }
        return false;
    }

    private boolean checkIfDifferent(ServerReponse response) {
        return this.lastTick != response.tick;
    }

    private boolean checkIfForMe(ServerReponse response) {
        return Objects.equals(response.id, this.id) || Objects.equals(response.id, this.hashedId) || Objects.equals(response.id, "all");
    }

    private void changeModule(String moduleId) {
        System.out.println("[" + this.hashedId + "] Setting Module: " + moduleId);
        this.config.requireConfig("general.current_module").setValue(moduleId);
    }

    private void changeMap(Integer mapId) {
        System.out.println("[" + this.hashedId + "] Setting Map: " + mapId);
        this.config.requireConfig("general.working_map").setValue(mapId);
    }

    private void changeProfile(String name) {
        System.out.println("[" + this.hashedId + "] Setting Profile: " + name);
        // this.main.configManager.loadConfig(name);
        // ((ConfigHandler) this.config).loadConfig(name);
        this.main.setConfig(name);
    }

    private void resetBotStats() {
        System.out.println("[" + this.hashedId + "] Reset Stats");
        this.stats.resetStats();
    }

    public boolean doAction() {
        System.out.println("[" + this.hashedId + "] Processing Action: " + this.response.action);
        switch (this.response.action) {
            case "stop":
                this.bot.setRunning(false);
                break;
            case "start":
                this.bot.setRunning(true);
                break;
            case "module":
                this.changeModule(this.response.parameter);
                break;
            case "map":
                this.changeMap(Integer.parseInt(this.response.parameter));
                break;
            case "profile":
                this.changeProfile(this.response.parameter);
                break;
            case "reset_bot_stats":
                this.resetBotStats();
                break;
            case "none":
                return false;
        }
        return true;
    }

    public static class ServerReponse {
        private final String action;
        private final String parameter;
        private final String id;
        private final long tick;

        public ServerReponse() {
            this.action = "none";
            this.id = "all";
            this.tick = System.currentTimeMillis();
            this.parameter = "";
        }

        static ServerReponse fromJson(String json) {
            return new Gson().fromJson(json, ServerReponse.class);
        }
    }
}
