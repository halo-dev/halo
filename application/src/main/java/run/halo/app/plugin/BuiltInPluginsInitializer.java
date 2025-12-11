package run.halo.app.plugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.ExtensionInitializedEvent;
import run.halo.app.infra.exception.PluginAlreadyExistsException;

@Slf4j
@Component
class BuiltInPluginsInitializer implements ApplicationListener<ExtensionInitializedEvent> {

    private static final String PRESETS_LOCATION_PATTERN = "classpath:/plugins/built-in/*.jar";

    private final ExtensionClient client;

    private final PluginService pluginService;

    private ResourcePatternResolver resourcePatternResolver;

    private PluginFinder pluginFinder;

    BuiltInPluginsInitializer(ExtensionClient client, PluginService pluginService) {
        this.client = client;
        this.pluginService = pluginService;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        this.pluginFinder = new YamlPluginFinder();
    }

    /**
     * Only for testing purpose.
     *
     * @param resourcePatternResolver resource pattern resolver
     */
    void setResourcePatternResolver(ResourcePatternResolver resourcePatternResolver) {
        Assert.notNull(resourcePatternResolver, "ResourcePatternResolver must not be null");
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * Only for testing purpose.
     *
     * @param pluginFinder plugin finder
     */
    void setPluginFinder(PluginFinder pluginFinder) {
        this.pluginFinder = pluginFinder;
    }

    @Override
    public void onApplicationEvent(ExtensionInitializedEvent event) {
        try {
            for (var resource : resourcePatternResolver.getResources(PRESETS_LOCATION_PATTERN)) {
                var filename = resource.getFilename();
                if (filename == null) {
                    continue;
                }
                var pluginPath = Path.of(resource.getURI());
                var preflightPlugin = pluginFinder.find(pluginPath);
                var pluginName = preflightPlugin.getMetadata().getName();

                log.info("Try to installing built-in plugin '{}'...", pluginName);
                var plugin = pluginService.install(pluginPath)
                    .doOnNext(created -> {
                        log.info("Built-in plugin '{}' has been installed.",
                            created.getMetadata().getName());
                    })
                    .onErrorResume(PluginAlreadyExistsException.class, e -> {
                        log.info("Built-in plugin '{}' already installed, trying to upgrade...",
                            pluginName);
                        return pluginService.upgrade(pluginName, pluginPath)
                            .doOnNext(updated -> log.info("Built-in plugin '{}' has been upgraded.",
                                updated.getMetadata().getName()));
                    })
                    .blockOptional(Duration.ofSeconds(10)).orElseThrow(
                        () -> new IllegalStateException(
                            "Failed to install or upgrade built-in plugin '" + pluginName + "'"
                        )
                    );
                // try to update metadata to add system reserved label and finalizer
                var metadata = plugin.getMetadata();
                metadata.setDeletionTimestamp(null);
                if (metadata.getLabels() == null) {
                    metadata.setLabels(new HashMap<>());
                }
                metadata.getLabels().put(Plugin.SYSTEM_RESERVED_LABEL_KEY, Boolean.TRUE.toString());
                if (metadata.getFinalizers() == null) {
                    metadata.setFinalizers(new HashSet<>());
                }
                metadata.getFinalizers().add(Plugin.BUILT_IN_KEEPER_FINALIZER);
                client.update(plugin);
            }
        } catch (FileNotFoundException ignored) {
            // should never happen
            log.warn("No built-in plugins found.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
