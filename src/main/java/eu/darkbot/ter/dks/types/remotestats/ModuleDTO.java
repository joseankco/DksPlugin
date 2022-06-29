package eu.darkbot.ter.dks.types.remotestats;

import eu.darkbot.api.managers.BotAPI;

public class ModuleDTO {
    private String status;
    public ModuleDTO(BotAPI botAPI) {
        this.status = botAPI.getModule().getStatus();
    }
}
