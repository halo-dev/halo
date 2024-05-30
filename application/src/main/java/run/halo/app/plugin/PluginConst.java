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

    String SYSTEM_PLUGIN_NAME = "system";

    String RELOAD_ANNO = "plugin.halo.run/reload";

    String REQUEST_TO_UNLOAD_LABEL = "plugin.halo.run/request-to-unload";

    String PLUGIN_PATH = "plugin.halo.run/plugin-path";

    String RUNTIME_MODE_ANNO = "plugin.halo.run/runtime-mode";

    static String assetsRoutePrefix(String pluginName) {
        return "/plugins/" + pluginName + "/assets/";
    }

}
