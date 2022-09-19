package run.halo.app.plugin;

import java.util.ArrayList;
import java.util.List;
import org.pf4j.ExtensionPoint;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Extension components finder for {@link ExtensionPoint}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ExtensionComponentsFinder {
    public static final String SYSTEM_PLUGIN_ID = "system";
    private final HaloPluginManager haloPluginManager;
    private final ApplicationContext applicationContext;

    public ExtensionComponentsFinder(HaloPluginManager haloPluginManager,
        ApplicationContext applicationContext) {
        this.haloPluginManager = haloPluginManager;
        this.applicationContext = applicationContext;
    }

    /**
     * Finds all extension components.
     *
     * @param type subclass type of extension point
     * @param <T> extension component type
     * @return extension components
     */
    public <T> List<T> getExtensions(Class<T> type) {
        assertExtensionPoint(type);
        List<T> components = new ArrayList<>(haloPluginManager.getExtensions(type));
        components.addAll(applicationContext.getBeansOfType(type).values());
        return List.copyOf(components);
    }

    /**
     * <p>Finds all extension components by plugin id.</p>
     * If the plugin id is system or null, it means to find from halo.
     *
     * @param type subclass type of extension point
     * @param <T> extension component type
     * @return extension components
     */
    public <T> List<T> getExtensions(Class<T> type, String pluginId) {
        assertExtensionPoint(type);
        List<T> components = new ArrayList<>();
        if (pluginId == null || SYSTEM_PLUGIN_ID.equals(pluginId)) {
            components.addAll(applicationContext.getBeansOfType(type).values());
            return components;
        } else {
            components.addAll(haloPluginManager.getExtensions(type, pluginId));
        }
        return components;
    }

    private void assertExtensionPoint(Class<?> type) {
        if (!ExtensionPoint.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("The type must be a subclass of ExtensionPoint");
        }
    }
}
