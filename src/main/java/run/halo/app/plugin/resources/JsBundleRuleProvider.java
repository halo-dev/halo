package run.halo.app.plugin.resources;

import java.util.Optional;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.ReverseProxy.FileReverseProxyProvider;
import run.halo.app.core.extension.ReverseProxy.ReverseProxyRule;
import run.halo.app.plugin.HaloPluginManager;

/**
 * TODO Optimize code to support user customize js bundle rules.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class JsBundleRuleProvider {
    private final HaloPluginManager haloPluginManager;

    public JsBundleRuleProvider(HaloPluginManager haloPluginManager) {
        this.haloPluginManager = haloPluginManager;
    }

    /**
     * Gets plugin js bundle rule.
     *
     * @param pluginName plugin name
     * @return a js bundle rule
     */
    public Optional<ReverseProxyRule> jsRule(String pluginName) {
        return Optional.of("/admin/main.js")
            .filter(path -> createResourceLoader(pluginName)
                .getResource(path).exists())
            .map(path -> {
                FileReverseProxyProvider
                    file = new FileReverseProxyProvider("admin", "main.js");
                return new ReverseProxyRule(path, file);
            });
    }

    /**
     * Gets plugin stylesheet rule.
     *
     * @param pluginName plugin name
     * @return a stylesheet bundle rule
     */
    public Optional<ReverseProxyRule> cssRule(String pluginName) {
        return Optional.of("/admin/style.css")
            .filter(path -> createResourceLoader(pluginName)
                .getResource(path)
                .exists())
            .map(path -> {
                FileReverseProxyProvider
                    file = new FileReverseProxyProvider("admin", "style.css");
                return new ReverseProxyRule(path, file);
            });
    }

    @NonNull
    private DefaultResourceLoader createResourceLoader(String pluginName) {
        PluginWrapper plugin = haloPluginManager.getPlugin(pluginName);
        if (plugin == null) {
            return new DefaultResourceLoader();
        }
        return new DefaultResourceLoader(plugin.getPluginClassLoader());
    }

}
