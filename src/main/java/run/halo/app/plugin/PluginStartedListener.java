package run.halo.app.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.event.HaloPluginStartedEvent;

/**
 * TODO Optimized Unstructured loading.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginStartedListener implements ApplicationListener<HaloPluginStartedEvent> {

    private final ExtensionClient extensionClient;

    public PluginStartedListener(ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(HaloPluginStartedEvent event) {
        PluginWrapper pluginWrapper = event.getPlugin();
        Plugin plugin =
            extensionClient.fetch(Plugin.class, pluginWrapper.getPluginId()).orElseThrow();
        // load unstructured
        DefaultResourceLoader resourceLoader =
            new DefaultResourceLoader(pluginWrapper.getPluginClassLoader());
        plugin.getSpec().getExtensionLocations()
            .stream()
            .map(resourceLoader::getResource)
            .filter(Resource::exists)
            .map(resource -> new YamlUnstructuredLoader(resource).load())
            .flatMap(List::stream)
            .forEach(unstructured -> {
                MetadataOperator metadata = unstructured.getMetadata();
                Map<String, String> labels = metadata.getLabels();
                if (labels == null) {
                    labels = new HashMap<>();
                    metadata.setLabels(labels);
                }
                labels.put(PluginConst.PLUGIN_NAME_LABEL_NAME, plugin.getMetadata().getName());
                extensionClient.fetch(
                        GroupVersionKind.fromAPIVersionAndKind(unstructured.getApiVersion(),
                            unstructured.getKind()), metadata.getName())
                    .ifPresentOrElse(persisted -> {
                        unstructured.getMetadata().setVersion(persisted.getMetadata().getVersion());
                        extensionClient.update(unstructured);
                    }, () -> extensionClient.create(unstructured));
            });
    }
}
