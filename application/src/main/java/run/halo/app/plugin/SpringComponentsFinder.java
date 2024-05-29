package run.halo.app.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.AbstractExtensionFinder;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginWrapper;
import org.pf4j.processor.ExtensionStorage;

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

    public SpringComponentsFinder(PluginManager pluginManager) {
        super(pluginManager);
        entries = new ConcurrentHashMap<>();
    }

    @Override
    public Map<String, Set<String>> readClasspathStorages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Set<String>> readPluginsStorages() {
        throw new UnsupportedOperationException();
    }

    private Set<String> readPluginStorage(PluginWrapper pluginWrapper) {
        var pluginId = pluginWrapper.getPluginId();
        log.debug("Reading extensions storage from plugin '{}'", pluginId);
        var bucket = new HashSet<String>();
        try {
            log.debug("Read '{}'", EXTENSIONS_RESOURCE);
            var classLoader = pluginWrapper.getPluginClassLoader();
            try (var resourceStream = classLoader.getResourceAsStream(EXTENSIONS_RESOURCE)) {
                if (resourceStream == null) {
                    log.debug("Cannot find '{}'", EXTENSIONS_RESOURCE);
                } else {
                    collectExtensions(resourceStream, bucket);
                }
            }
            debugExtensions(bucket);
        } catch (IOException e) {
            log.error("Failed to read components from " + EXTENSIONS_RESOURCE, e);
        }
        return bucket;
    }

    private void collectExtensions(InputStream inputStream, Set<String> bucket) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            ExtensionStorage.read(reader, bucket);
        }
    }

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        var pluginState = event.getPluginState();
        String pluginId = event.getPlugin().getPluginId();
        if (pluginState == PluginState.UNLOADED) {
            entries.remove(pluginId);
        } else if (pluginState == PluginState.CREATED || pluginState == PluginState.RESOLVED) {
            entries.computeIfAbsent(pluginId, id -> readPluginStorage(event.getPlugin()));
        }
    }

}
