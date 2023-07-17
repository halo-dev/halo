package run.halo.app.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.lang.NonNull;

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

    private final Map<String, PluginApplicationContext> registry = new HashMap<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public static ExtensionContextRegistry getInstance() {
        return INSTANCE;
    }

    private ExtensionContextRegistry() {
    }

    /**
     * Acquire the read lock when using getPluginApplicationContexts and getByPluginId.
     */
    public void acquireReadLock() {
        this.readWriteLock.readLock().lock();
    }

    /**
     * Release the read lock after using getPluginApplicationContexts and getByPluginId.
     */
    public void releaseReadLock() {
        this.readWriteLock.readLock().unlock();
    }

    /**
     * Register plugin application context to registry map.
     *
     * @param pluginId plugin id(name)
     * @param context plugin application context
     */
    public void register(String pluginId, PluginApplicationContext context) {
        this.readWriteLock.writeLock().lock();
        try {
            registry.put(pluginId, context);
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Remove plugin application context from registry map.
     *
     * @param pluginId plugin id
     */
    public void remove(String pluginId) {
        this.readWriteLock.writeLock().lock();
        try {
            PluginApplicationContext removed = registry.remove(pluginId);
            if (removed != null) {
                removed.close();
            }
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Gets plugin application context by plugin id from registry map.
     * Note: ensure call {@link #containsContext(String)} after call this method.
     *
     * @param pluginId plugin id
     * @return plugin application context
     * @throws IllegalArgumentException if plugin id not found in registry
     */
    @NonNull
    public PluginApplicationContext getByPluginId(String pluginId) {
        this.readWriteLock.readLock().lock();
        try {
            PluginApplicationContext context = registry.get(pluginId);
            if (context == null) {
                throw new IllegalArgumentException(
                    String.format("The plugin [%s] can not be found.", pluginId));
            }
            return context;
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    /**
     * Check whether the registry contains the plugin application context by plugin id.
     *
     * @param pluginId plugin id
     * @return true if contains, otherwise false
     */
    public boolean containsContext(String pluginId) {
        this.readWriteLock.readLock().lock();
        try {
            return registry.containsKey(pluginId);
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    /**
     * Gets all plugin application contexts from registry map.
     *
     * @return plugin application contexts
     */
    public List<PluginApplicationContext> getPluginApplicationContexts() {
        this.readWriteLock.readLock().lock();
        try {
            return new ArrayList<>(registry.values());
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }
}
