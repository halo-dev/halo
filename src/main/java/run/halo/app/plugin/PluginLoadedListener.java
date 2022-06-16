package run.halo.app.plugin;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.event.HaloPluginLoadedEvent;
import run.halo.app.plugin.resources.ReverseProxy;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginLoadedListener implements ApplicationListener<HaloPluginLoadedEvent> {
    private static final String REVERSE_PROXY_NAME = "extensions/reverseProxy.yaml";
    private final ExtensionClient extensionClient;

    public PluginLoadedListener(ExtensionClient extensionClient, SchemeManager schemeManager) {
        this.extensionClient = extensionClient;

        // TODO Optimize schemes register
        schemeManager.register(Plugin.class);
        schemeManager.register(ReverseProxy.class);
    }

    @Override
    public void onApplicationEvent(HaloPluginLoadedEvent event) {
        PluginWrapper pluginWrapper = event.getPluginWrapper();
        // TODO: Optimize plugin custom resource loading method
        // load plugin.yaml
        YamlPluginFinder yamlPluginFinder = new YamlPluginFinder();
        Plugin plugin = yamlPluginFinder.find(pluginWrapper.getPluginPath());
        DefaultResourceLoader defaultResourceLoader =
            new DefaultResourceLoader(pluginWrapper.getPluginClassLoader());
        extensionClient.create(plugin);
        // load reverse proxy
        Resource resource = defaultResourceLoader.getResource(REVERSE_PROXY_NAME);
        if (resource.exists()) {
            YamlUnstructuredLoader unstructuredLoader = new YamlUnstructuredLoader(resource);
            unstructuredLoader.load().forEach(extensionClient::create);
        }
    }
}
