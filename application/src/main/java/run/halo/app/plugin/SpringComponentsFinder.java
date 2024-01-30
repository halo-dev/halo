package run.halo.app.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.AbstractExtensionFinder;
import org.pf4j.PluginManager;
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
        // We have to copy the source code from `org.pf4j.LegacyExtensionFinder.readPluginsStorages`
        // because we have to adapt to the new extensions resource location
        // `META-INF/plugin-components.idx`.
        log.debug("Reading components storages from plugins");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        List<PluginWrapper> plugins = pluginManager.getPlugins();
        for (PluginWrapper plugin : plugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            log.debug("Reading extensions storage from plugin '{}'", pluginId);
            Set<String> bucket = new HashSet<>();

            try {
                log.debug("Read '{}'", EXTENSIONS_RESOURCE);
                ClassLoader pluginClassLoader = plugin.getPluginClassLoader();
                try (var resourceStream =
                         pluginClassLoader.getResourceAsStream(EXTENSIONS_RESOURCE)) {
                    if (resourceStream == null) {
                        log.debug("Cannot find '{}'", EXTENSIONS_RESOURCE);
                    } else {
                        collectExtensions(resourceStream, bucket);
                    }
                }

                debugExtensions(bucket);

                result.put(pluginId, bucket);
            } catch (IOException e) {
                log.error("Failed to read components from " + EXTENSIONS_RESOURCE, e);
            }
        }

        return result;
    }

    private void collectExtensions(InputStream inputStream, Set<String> bucket) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            ExtensionStorage.read(reader, bucket);
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

}

