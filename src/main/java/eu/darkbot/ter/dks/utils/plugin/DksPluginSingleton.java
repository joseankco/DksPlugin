package eu.darkbot.ter.dks.utils.plugin;

public class DksPluginSingleton {
    private static DksPluginInfo plugin;

    public static DksPluginInfo getPluginInfo() {
        if (plugin == null) {
            plugin = new DksPluginInfo();
        }
        return plugin;
    }
}
