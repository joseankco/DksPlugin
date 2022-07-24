package eu.darkbot.ter.dks.types.remotestats;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.core.manager.StarManager;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.ConfigAPI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigDTO {
    private final List<ModuleOptionsDTO> moduleOptions;
    private final List<MapOptionsDTO> mapOptions;
    private final List<String> profileOptions;

    private final String selectedModuleId;
    private final Integer selectedMapId;
    private final String selectedProfile;

    public ConfigDTO(ConfigAPI config, Main main) {
        this.moduleOptions = new ArrayList<>();
        this.mapOptions = new ArrayList<>();

        @NotNull ConfigSetting<Object> mocs = config.requireConfig("general.current_module");
        this.selectedModuleId = (String) mocs.getValue();
        ModuleSupplier ms = mocs.getMetadata("dropdown.options");
        if (ms != null) {
            ms.options().forEach(o -> this.moduleOptions.add(new ModuleOptionsDTO(ms, o)));
        }

        @NotNull ConfigSetting<Object> macs = config.requireConfig("general.working_map");
        this.selectedMapId = (Integer) macs.getValue();
        StarManager.MapOptions mo = macs.getMetadata("dropdown.options");
        if (mo != null) {
            mo.options().forEach(o -> this.mapOptions.add(new MapOptionsDTO(mo, o)));
        }

        this.selectedProfile = main.configManager.getConfigName();
        this.profileOptions = main.configManager.getAvailableConfigs();
    }

    public static class ModuleOptionsDTO {
        public String value;
        public String name;
        public ModuleOptionsDTO(ModuleSupplier ms, String option) {
            this.value = option;
            this.name = ms.getText(option);
        }
    }

    public static class MapOptionsDTO {
        public Integer value;
        public String name;
        public MapOptionsDTO(StarManager.MapOptions mo, Integer option) {
            this.value = option;
            this.name = mo.getText(option);
        }
    }
}
