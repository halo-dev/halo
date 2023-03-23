package run.halo.app.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Plugin application context registrar.</p>
 * <p>It contains a map, the key is the plugin id and the value is application context of plugin
 * .</p>
 * <p>when the plugin is enabled, an application context will be registered in the map by plugin id.
 * it will be deleted according to its id when the plugin is disabled.</p>
 *
 * @author guqing
 * @since 2021-11-15
 */
public class ExtensionContextRegistry {
    private static final ExtensionContextRegistry INSTANCE = new ExtensionContextRegistry();

    private final Map<String, PluginApplicationContext> registry = new ConcurrentHashMap<>();

    public static ExtensionContextRegistry getInstance() {
        return INSTANCE;
    }

    private ExtensionContextRegistry() {
    }

    public void register(String pluginId, PluginApplicationContext context) {
        registry.put(pluginId, context);
    }

    public PluginApplicationContext remove(String pluginId) {
        return registry.remove(pluginId);
    }

    /**
     * Gets plugin application context by plugin id from registry map.
     *
     * @param pluginId plugin id
     * @return plugin application context
     * @throws IllegalArgumentException if plugin id not found in registry
     */
    public PluginApplicationContext getByPluginId(String pluginId) {
        PluginApplicationContext context = registry.get(pluginId);
        if (context == null) {
            throw new IllegalArgumentException(
                String.format("The plugin [%s] can not be found.", pluginId));
        }
        return context;
    }

    public boolean containsContext(String pluginId) {
        return registry.containsKey(pluginId);
    }

    public List<PluginApplicationContext> getPluginApplicationContexts() {
        return new ArrayList<>(registry.values());
    }
}
