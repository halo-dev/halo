package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginLoader;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.pf4j.VersionManager;

/**
 * PluginManager to hold the main ApplicationContext.
 * It provides methods for managing the plugin lifecycle.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class HaloPluginManager extends UnSafePluginManager {

    static final ReentrantLock LOCK = new ReentrantLock();

    public HaloPluginManager() {
        super();
    }

    public HaloPluginManager(Path pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    public void setSystemVersion(String version) {
        LOCK.lock();
        try {
            super.setSystemVersion(version);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public String getSystemVersion() {
        LOCK.lock();
        try {
            return super.getSystemVersion();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public List<PluginWrapper> getPlugins() {
        LOCK.lock();
        try {
            return super.getPlugins();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public List<PluginWrapper> getPlugins(PluginState pluginState) {
        LOCK.lock();
        try {
            return super.getPlugins(pluginState);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public List<PluginWrapper> getResolvedPlugins() {
        LOCK.lock();
        try {
            return super.getResolvedPlugins();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public List<PluginWrapper> getUnresolvedPlugins() {
        LOCK.lock();
        try {
            return super.getUnresolvedPlugins();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public List<PluginWrapper> getStartedPlugins() {
        LOCK.lock();
        try {
            return super.getStartedPlugins();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public PluginWrapper getPlugin(String pluginId) {
        LOCK.lock();
        try {
            return super.getPlugin(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public String loadPlugin(Path pluginPath) {
        LOCK.lock();
        try {
            return super.loadPlugin(pluginPath);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void loadPlugins() {
        LOCK.lock();
        try {
            super.loadPlugins();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void unloadPlugins() {
        LOCK.lock();
        try {
            super.unloadPlugins();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public boolean unloadPlugin(String pluginId) {
        LOCK.lock();
        try {
            return super.unloadPlugin(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public boolean deletePlugin(String pluginId) {
        LOCK.lock();
        try {
            return super.deletePlugin(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void startPlugins() {
        LOCK.lock();
        try {
            super.startPlugins();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public PluginState startPlugin(String pluginId) {
        LOCK.lock();
        try {
            return super.startPlugin(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void stopPlugins() {
        LOCK.lock();
        try {
            super.stopPlugins();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public PluginState stopPlugin(String pluginId) {
        LOCK.lock();
        try {
            return super.stopPlugin(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public boolean disablePlugin(String pluginId) {
        LOCK.lock();
        try {
            return super.disablePlugin(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public boolean enablePlugin(String pluginId) {
        LOCK.lock();
        try {
            return super.enablePlugin(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public ClassLoader getPluginClassLoader(String pluginId) {
        LOCK.lock();
        try {
            return super.getPluginClassLoader(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public List<Class<?>> getExtensionClasses(String pluginId) {
        LOCK.lock();
        try {
            return super.getExtensionClasses(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public <T> List<Class<? extends T>> getExtensionClasses(Class<T> type) {
        LOCK.lock();
        try {
            return super.getExtensionClasses(type);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public <T> List<Class<? extends T>> getExtensionClasses(Class<T> type, String pluginId) {
        LOCK.lock();
        try {
            return super.getExtensionClasses(type, pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type) {
        LOCK.lock();
        try {
            return super.getExtensions(type);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type, String pluginId) {
        LOCK.lock();
        try {
            return super.getExtensions(type, pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public List getExtensions(String pluginId) {
        LOCK.lock();
        try {
            return super.getExtensions(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public Set<String> getExtensionClassNames(String pluginId) {
        LOCK.lock();
        try {
            return super.getExtensionClassNames(pluginId);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public ExtensionFactory getExtensionFactory() {
        LOCK.lock();
        try {
            return super.getExtensionFactory();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public PluginLoader getPluginLoader() {
        LOCK.lock();
        try {
            return super.getPluginLoader();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public Path getPluginsRoot() {
        LOCK.lock();
        try {
            return super.getPluginsRoot();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public List<Path> getPluginsRoots() {
        LOCK.lock();
        try {
            return super.getPluginsRoots();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public RuntimeMode getRuntimeMode() {
        LOCK.lock();
        try {
            return super.getRuntimeMode();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public PluginWrapper whichPlugin(Class<?> clazz) {
        LOCK.lock();
        try {
            return super.whichPlugin(clazz);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public boolean isExactVersionAllowed() {
        LOCK.lock();
        try {
            return super.isExactVersionAllowed();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void setExactVersionAllowed(boolean exactVersionAllowed) {
        LOCK.lock();
        try {
            super.setExactVersionAllowed(exactVersionAllowed);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public VersionManager getVersionManager() {
        LOCK.lock();
        try {
            return super.getVersionManager();
        } finally {
            LOCK.unlock();
        }
    }
}
