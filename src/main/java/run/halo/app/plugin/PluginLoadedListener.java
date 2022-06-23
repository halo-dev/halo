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
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.event.HaloPluginLoadedEvent;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginLoadedListener implements ApplicationListener<HaloPluginLoadedEvent> {
    private final ExtensionClient extensionClient;

    public PluginLoadedListener(ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(HaloPluginLoadedEvent event) {
        PluginWrapper pluginWrapper = event.getPluginWrapper();
        // TODO: Optimize plugin custom resource loading method
        // load plugin.yaml
        YamlPluginFinder yamlPluginFinder = new YamlPluginFinder();
        Plugin plugin = yamlPluginFinder.find(pluginWrapper.getPluginPath());
        extensionClient.create(plugin);

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
                Map<String, String> labels = unstructured.getMetadata().getLabels();
                if (labels == null) {
                    unstructured.getMetadata().setLabels(new HashMap<>());
                }
                unstructured.getMetadata()
                    .getLabels()
                    .put(PluginConst.PLUGIN_NAME_LABEL_NAME, plugin.getMetadata().getName());
                extensionClient.create(unstructured);
            });
    }
}
