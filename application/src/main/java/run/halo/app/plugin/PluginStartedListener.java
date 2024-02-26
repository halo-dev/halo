package run.halo.app.plugin;

import static run.halo.app.plugin.PluginConst.PLUGIN_NAME_LABEL_NAME;
import static run.halo.app.plugin.PluginExtensionLoaderUtils.isSetting;
import static run.halo.app.plugin.PluginExtensionLoaderUtils.lookupExtensions;

import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.event.HaloPluginStartedEvent;

/**
 * TODO Optimized Unstructured loading.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class PluginStartedListener {

    private final ReactiveExtensionClient client;

    public PluginStartedListener(ReactiveExtensionClient extensionClient) {
        this.client = extensionClient;
    }

    private Mono<Unstructured> createOrUpdate(Unstructured unstructured) {
        var name = unstructured.getMetadata().getName();
        return client.fetch(unstructured.groupVersionKind(), name)
            .doOnNext(old -> {
                unstructured.getMetadata().setVersion(old.getMetadata().getVersion());
            })
            .map(ignored -> unstructured)
            .flatMap(client::update)
            .switchIfEmpty(Mono.defer(() -> client.create(unstructured)));
    }

    @EventListener
    public Mono<Void> onApplicationEvent(HaloPluginStartedEvent event) {
        var pluginWrapper = event.getPlugin();
        var p = pluginWrapper.getPlugin();
        if (!(p instanceof SpringPlugin springPlugin)) {
            return Mono.empty();
        }
        var applicationContext = springPlugin.getApplicationContext();
        if (!(applicationContext instanceof PluginApplicationContext pluginApplicationContext)) {
            return Mono.empty();
        }
        var pluginName = pluginWrapper.getPluginId();

        return client.get(Plugin.class, pluginName)
            .flatMap(plugin -> Flux.fromStream(
                    () -> {
                        log.debug("Collecting extensions for plugin {}", pluginName);
                        var resources = lookupExtensions(pluginWrapper.getPluginClassLoader());
                        var loader = new YamlUnstructuredLoader(resources);
                        var settingName = plugin.getSpec().getSettingName();
                        // TODO The load method may be over memory consumption.
                        return loader.load()
                            .stream()
                            .filter(isSetting(settingName).negate());
                    })
                .doOnNext(unstructured -> {
                    var name = unstructured.getMetadata().getName();
                    pluginApplicationContext
                        .addExtensionMapping(unstructured.groupVersionKind(), name);
                    var labels = unstructured.getMetadata().getLabels();
                    if (labels == null) {
                        labels = new HashMap<>();
                        unstructured.getMetadata().setLabels(labels);
                    }
                    labels.put(PLUGIN_NAME_LABEL_NAME, plugin.getMetadata().getName());
                })
                .flatMap(this::createOrUpdate)
                .then());
    }
}
