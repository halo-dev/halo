package run.halo.app.plugin.resources;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.PathUtils;

/**
 * Plugin bundle resources utils.
 *
 * @author guqing
 * @since 2.0.0
 */
public abstract class BundleResourceUtils {
    private static final String CONSOLE_BUNDLE_LOCATION = "console";
    public static final String JS_BUNDLE = "main.js";
    public static final String CSS_BUNDLE = "style.css";

    /**
     * Gets js bundle resource by plugin name in console location.
     *
     * @return js bundle resource if exists, otherwise null
     */
    @Nullable
    public static Resource getJsBundleResource(PluginManager pluginManager, String pluginName,
        String bundleName) {
        Assert.hasText(pluginName, "The pluginName must not be blank");
        Assert.hasText(bundleName, "Bundle name must not be blank");

        DefaultResourceLoader resourceLoader = getResourceLoader(pluginManager, pluginName);
        if (resourceLoader == null) {
            return null;
        }
        String path = PathUtils.combinePath(CONSOLE_BUNDLE_LOCATION, bundleName);
        String simplifyPath = StringUtils.cleanPath(path);
        FileUtils.checkDirectoryTraversal("/" + CONSOLE_BUNDLE_LOCATION, simplifyPath);
        Resource resource = resourceLoader.getResource(simplifyPath);
        return resource.exists() ? resource : null;
    }

    @Nullable
    public static DefaultResourceLoader getResourceLoader(PluginManager pluginManager,
        String pluginName) {
        Assert.notNull(pluginManager, "Plugin manager must not be null");
        PluginWrapper plugin = pluginManager.getPlugin(pluginName);
        if (plugin == null) {
            return null;
        }
        return new DefaultResourceLoader(plugin.getPluginClassLoader());
    }
}
