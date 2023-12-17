package run.halo.app.plugin;

/**
 * Plugin constants.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface PluginConst {
    /**
     * Plugin metadata labels key.
     */
    String PLUGIN_NAME_LABEL_NAME = "plugin.halo.run/plugin-name";

    String DELETE_STAGE = "delete-stage";

    String SYSTEM_PLUGIN_NAME = "system";

    String RELOAD_ANNO = "plugin.halo.run/reload";

    String PLUGIN_PATH = "plugin.halo.run/plugin-path";

    static String assertsRoutePrefix(String pluginName) {
        return "/plugins/" + pluginName + "/assets/";
    }

    enum DeleteStage {
        STOP,
        UNINSTALL
    }
}
