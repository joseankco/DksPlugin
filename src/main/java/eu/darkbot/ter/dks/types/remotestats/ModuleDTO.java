package eu.darkbot.ter.dks.types.remotestats;

import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;

import eu.darkbot.api.extensions.FeatureInfo;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.ExtensionsAPI;

public class ModuleDTO {
    private String status;
    private String id;
    private String name;
    private String description;

    public ModuleDTO(BotAPI botAPI, ExtensionsAPI extensions, ConfigAPI config) {
        this.status = botAPI.getModule().getStatus();
        this.id = (String) config.requireConfig("general.current_module").getValue();
        FeatureInfo<Object> info = ((FeatureRegistry) extensions).getFeatureInfo(this.id);
        this.name = info.getName();
        this.description = info.getDescription();
    }
}
