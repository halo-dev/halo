package run.halo.app.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.AbstractExtensionFinder;
import org.pf4j.PluginDependency;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginWrapper;
import org.pf4j.processor.ExtensionStorage;
import org.springframework.util.Assert;

/**
 * <p>The spring component finder. it will read {@code META-INF/plugin-components.idx} file in
 * plugin to obtain the class name that needs to be registered in the plugin IOC.</p>
 * <p>Reading index files directly is much faster than dynamically scanning class components when
 * the plugin is enabled.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class SpringComponentsFinder extends AbstractExtensionFinder {
    public static final String EXTENSIONS_RESOURCE = "META-INF/plugin-components.idx";
    private final StampedLock entryStampedLock = new StampedLock();

    public SpringComponentsFinder(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    public Map<String, Set<String>> readClasspathStorages() {
        log.debug("Reading components storages from classpath");
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Set<String>> readPluginsStorages() {
        log.debug("Reading components storages from plugins");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        List<PluginWrapper> plugins = pluginManager.getPlugins();
        for (PluginWrapper plugin : plugins) {
            readPluginStorageToMemory(plugin);
        }

        return result;
    }

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        // see supper class for more details
        if (checkForExtensionDependencies == null && PluginState.STARTED.equals(
            event.getPluginState())) {
            for (PluginDependency dependency : event.getPlugin().getDescriptor()
                .getDependencies()) {
                if (dependency.isOptional()) {
                    log.debug("Enable check for extension dependencies via ASM.");
                    checkForExtensionDependencies = true;
                    break;
                }
            }
        }
    }

    private void collectExtensions(InputStream inputStream, Set<String> bucket) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            ExtensionStorage.read(reader, bucket);
        }
    }

    protected void readPluginStorageToMemory(PluginWrapper pluginWrapper) {
        String pluginId = pluginWrapper.getPluginId();
        if (containsComponentsStorage(pluginId)) {
            return;
        }
        log.debug("Reading components storage from plugin '{}'", pluginId);
        Set<String> bucket = new HashSet<>();

        try {
            log.debug("Read '{}'", EXTENSIONS_RESOURCE);
            ClassLoader pluginClassLoader = pluginWrapper.getPluginClassLoader();
            try (InputStream resourceStream = pluginClassLoader.getResourceAsStream(
                EXTENSIONS_RESOURCE)) {
                if (resourceStream == null) {
                    log.debug("Cannot find '{}'", EXTENSIONS_RESOURCE);
                } else {
                    collectExtensions(resourceStream, bucket);
                }
            }

            debugExtensions(bucket);

            putComponentsStorage(pluginId, bucket);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected boolean containsComponentsStorage(String pluginId) {
        Assert.notNull(pluginId, "The pluginId cannot be null");
        long stamp = entryStampedLock.tryOptimisticRead();
        boolean contains = super.entries != null && super.entries.containsKey(pluginId);
        if (!entryStampedLock.validate(stamp)) {
            stamp = entryStampedLock.readLock();
            try {
                return super.entries != null && entries.containsKey(pluginId);
            } finally {
                entryStampedLock.unlockRead(stamp);
            }
        }
        return contains;
    }

    protected void putComponentsStorage(String pluginId, Set<String> components) {
        Assert.notNull(pluginId, "The pluginId cannot be null");
        // When the lock remains in write mode, the read lock cannot be obtained
        long stamp = entryStampedLock.writeLock();
        try {
            Map<String, Set<String>> componentNamesMap;
            if (super.entries == null) {
                componentNamesMap = new HashMap<>();
            } else {
                componentNamesMap = new HashMap<>(super.entries);
            }
            log.debug("Load [{}] component names into storage cache for plugin [{}].",
                components.size(), pluginId);
            componentNamesMap.put(pluginId, components);
            super.entries = componentNamesMap;
        } finally {
            entryStampedLock.unlockWrite(stamp);
        }
    }

    protected void removeComponentsStorage(String pluginId) {
        Assert.notNull(pluginId, "The pluginId cannot be null");
        long stamp = entryStampedLock.writeLock();
        try {
            Map<String, Set<String>> componentNamesMap;
            if (super.entries == null) {
                componentNamesMap = new HashMap<>();
            } else {
                componentNamesMap = new HashMap<>(super.entries);
            }
            log.debug("Removing components storage from cache [{}].", pluginId);
            componentNamesMap.remove(pluginId);
            super.entries = componentNamesMap;
        } finally {
            entryStampedLock.unlockWrite(stamp);
        }
    }
}

