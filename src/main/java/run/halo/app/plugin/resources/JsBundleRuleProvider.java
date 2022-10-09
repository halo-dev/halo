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
    private static final String JS_LOCATION = "/console/main.js";
    private static final String CSS_LOCATION = "/console/style.css";

    private static final FileReverseProxyProvider JS_FILE_PROXY =
        new FileReverseProxyProvider("console", "main.js");

    private static final FileReverseProxyProvider CSS_FILE_PROXY =
        new FileReverseProxyProvider("console", "style.css");

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
        return Optional.of(JS_LOCATION)
            .filter(path -> createResourceLoader(pluginName)
                .getResource(path).exists())
            .map(path -> new ReverseProxyRule(path, JS_FILE_PROXY));
    }

    /**
     * Gets plugin stylesheet rule.
     *
     * @param pluginName plugin name
     * @return a stylesheet bundle rule
     */
    public Optional<ReverseProxyRule> cssRule(String pluginName) {
        return Optional.of(CSS_LOCATION)
            .filter(path -> createResourceLoader(pluginName)
                .getResource(path)
                .exists())
            .map(path -> new ReverseProxyRule(path, CSS_FILE_PROXY));
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
