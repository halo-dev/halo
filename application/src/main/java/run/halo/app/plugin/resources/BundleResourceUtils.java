package run.halo.app.plugin.resources;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
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
    public static final String UI_BUNDLE_LOCATION = "ui";
    public static final String CONSOLE_BUNDLE_LOCATION = "console";
    public static final String JS_BUNDLE = "main.js";
    public static final String CSS_BUNDLE = "style.css";
    private static final String[] BUNDLE_LOCATIONS = {UI_BUNDLE_LOCATION, CONSOLE_BUNDLE_LOCATION};

    /**
     * Gets bundle resource by plugin name in the selected location.
     *
     * @return bundle resource if exists, otherwise null
     */
    public static @Nullable Resource getSelectedBundleResource(
            PluginManager pluginManager, String pluginName, String bundleName) {
        Assert.hasText(pluginName, "The pluginName must not be blank");
        Assert.hasText(bundleName, "Bundle name must not be blank");

        DefaultResourceLoader resourceLoader = getResourceLoader(pluginManager, pluginName);
        if (resourceLoader == null) {
            return null;
        }
        var bundleLocation = selectBundleLocation(resourceLoader);
        if (bundleLocation == null) {
            return null;
        }
        return getBundleResource(resourceLoader, bundleLocation, bundleName);
    }

    /**
     * Gets bundle resource by plugin name in the selected location.
     *
     * @return bundle resource if exists, otherwise null
     * @deprecated use {@link #getSelectedBundleResource(PluginManager, String, String)} instead
     */
    @Deprecated
    public static @Nullable Resource getJsBundleResource(
            PluginManager pluginManager, String pluginName, String bundleName) {
        return getSelectedBundleResource(pluginManager, pluginName, bundleName);
    }

    public static @Nullable Resource getBundleResource(
            PluginManager pluginManager, String pluginName, String bundleLocation, String bundleName) {
        Assert.hasText(pluginName, "The pluginName must not be blank");
        Assert.hasText(bundleLocation, "Bundle location must not be blank");
        Assert.hasText(bundleName, "Bundle name must not be blank");

        DefaultResourceLoader resourceLoader = getResourceLoader(pluginManager, pluginName);
        if (resourceLoader == null) {
            return null;
        }
        return getBundleResource(resourceLoader, bundleLocation, bundleName);
    }

    public static @Nullable String selectBundleLocation(PluginManager pluginManager, String pluginName) {
        Assert.hasText(pluginName, "The pluginName must not be blank");

        DefaultResourceLoader resourceLoader = getResourceLoader(pluginManager, pluginName);
        if (resourceLoader == null) {
            return null;
        }
        return selectBundleLocation(resourceLoader);
    }

    /**
     * Selects the bundle location for the given resource loader.
     *
     * <p>A location is considered available when the plugin provides at least one known UI bundle resource in that
     * directory, either {@value #JS_BUNDLE} or {@value #CSS_BUNDLE}.
     *
     * @param resourceLoader the resource loader to use
     * @return the selected bundle location, or {@code null} if no location is available
     */
    public static @Nullable String selectBundleLocation(DefaultResourceLoader resourceLoader) {
        Assert.notNull(resourceLoader, "Resource loader must not be null");
        for (String location : BUNDLE_LOCATIONS) {
            var jsBundle = getBundleResource(resourceLoader, location, JS_BUNDLE);
            var cssBundle = getBundleResource(resourceLoader, location, CSS_BUNDLE);
            if (jsBundle != null || cssBundle != null) {
                return location;
            }
        }
        return null;
    }

    public static @Nullable Resource getBundleResource(
            DefaultResourceLoader resourceLoader, String bundleLocation, String bundleName) {
        Assert.notNull(resourceLoader, "Resource loader must not be null");
        assertSupportedBundleLocation(bundleLocation);
        String path = PathUtils.combinePath(bundleLocation, bundleName);
        String simplifyPath = StringUtils.cleanPath(path);
        FileUtils.checkDirectoryTraversal("/" + bundleLocation, simplifyPath);
        Resource resource = resourceLoader.getResource(simplifyPath);
        return resource.exists() ? resource : null;
    }

    private static void assertSupportedBundleLocation(String bundleLocation) {
        for (String supportedLocation : BUNDLE_LOCATIONS) {
            if (supportedLocation.equals(bundleLocation)) {
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported bundle location: " + bundleLocation);
    }

    public static @Nullable DefaultResourceLoader getResourceLoader(PluginManager pluginManager, String pluginName) {
        Assert.notNull(pluginManager, "Plugin manager must not be null");
        PluginWrapper plugin = pluginManager.getPlugin(pluginName);
        if (plugin == null) {
            return null;
        }
        return new PluginResourceLoader(plugin.getPluginClassLoader());
    }

    private static class PluginResourceLoader extends DefaultResourceLoader {
        private final ClassLoader pluginClassLoader;

        PluginResourceLoader(ClassLoader pluginClassLoader) {
            super(pluginClassLoader);
            this.pluginClassLoader = pluginClassLoader;
        }

        @Override
        public Resource getResource(String location) {
            if (location.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
                return getResourceByPath(location.substring(ResourceLoader.CLASSPATH_URL_PREFIX.length()));
            }
            return super.getResource(location);
        }

        @Override
        protected Resource getResourceByPath(String path) {
            var resource = getOwnClasspathResource(path);
            return resource == null ? super.getResourceByPath(path) : resource;
        }

        private @Nullable Resource getOwnClasspathResource(String path) {
            if (!(pluginClassLoader instanceof URLClassLoader urlClassLoader)) {
                return null;
            }
            var urls = urlClassLoader.getURLs();
            if (urls == null || urls.length == 0) {
                return null;
            }
            var resourcePath = StringUtils.trimLeadingCharacter(path, '/');
            for (URL url : urls) {
                var resource = getResourceFromClasspathUrl(url, resourcePath);
                if (resource.exists()) {
                    return resource;
                }
            }
            return new DescriptiveResource("Plugin resource [" + resourcePath + "] does not exist");
        }

        private Resource getResourceFromClasspathUrl(URL classpathUrl, String resourcePath) {
            try {
                if (ResourceUtils.isJarURL(classpathUrl)) {
                    return new UrlResource(new URL(classpathUrl, resourcePath));
                }
                if (ResourceUtils.isFileURL(classpathUrl)) {
                    Path path = Path.of(classpathUrl.toURI());
                    if (Files.isDirectory(path)) {
                        return new FileSystemResource(path.resolve(resourcePath));
                    }
                    return new UrlResource(new URL("jar:" + classpathUrl.toExternalForm() + "!/" + resourcePath));
                }
                return new UrlResource(new URL(classpathUrl, resourcePath));
            } catch (Exception e) {
                return new DescriptiveResource(
                        "Plugin resource [" + resourcePath + "] from [" + classpathUrl + "] is invalid");
            }
        }
    }
}
