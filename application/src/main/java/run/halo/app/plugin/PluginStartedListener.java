package run.halo.app.plugin;

import static run.halo.app.plugin.PluginExtensionLoaderUtils.lookupExtensions;

import java.util.HashMap;
import org.pf4j.PluginWrapper;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.event.HaloPluginStartedEvent;

/**
 * TODO Optimized Unstructured loading.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginStartedListener {

    private final ReactiveExtensionClient client;

    public PluginStartedListener(ReactiveExtensionClient extensionClient) {
        this.client = extensionClient;
    }

    @EventListener
    public Mono<Void> onApplicationEvent(HaloPluginStartedEvent event) {
        PluginWrapper pluginWrapper = event.getPlugin();
        var resourceLoader =
            new DefaultResourceLoader(pluginWrapper.getPluginClassLoader());
        var pluginApplicationContext = ExtensionContextRegistry.getInstance()
            .getByPluginId(pluginWrapper.getPluginId());
        return client.get(Plugin.class, pluginWrapper.getPluginId())
            .zipWith(Mono.just(
                lookupExtensions(pluginWrapper.getPluginPath(), pluginWrapper.getRuntimeMode())))
            .flatMap(tuple2 -> {
                var plugin = tuple2.getT1();
                var extensionLocations = tuple2.getT2();
                return Flux.fromIterable(extensionLocations)
                    .map(resourceLoader::getResource)
                    .filter(Resource::exists)
                    .map(resource -> new YamlUnstructuredLoader(resource).load())
                    .flatMapIterable(rs -> rs)
                    .flatMap(unstructured -> {
                        var metadata = unstructured.getMetadata();
                        // collector plugin initialize extension resources
                        pluginApplicationContext.addExtensionMapping(
                            unstructured.groupVersionKind(),
                            metadata.getName());
                        var labels = metadata.getLabels();
                        if (labels == null) {
                            labels = new HashMap<>();
                        }
                        labels.put(PluginConst.PLUGIN_NAME_LABEL_NAME,
                            plugin.getMetadata().getName());
                        metadata.setLabels(labels);

                        return client.fetch(unstructured.groupVersionKind(), metadata.getName())
                            .flatMap(extension -> {
                                unstructured.getMetadata()
                                    .setVersion(extension.getMetadata().getVersion());
                                return client.update(unstructured);
                            })
                            .switchIfEmpty(Mono.defer(() -> client.create(unstructured)));
                    }).then();
            }).then();
    }
}
