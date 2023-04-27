package run.halo.app.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DevelopmentPluginClasspath;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginState;
import org.pf4j.util.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * <p>Reading plugin descriptor data from plugin.yaml.</p>
 * Example:
 * <pre>
 * apiVersion: v1alpha1
 * kind: Plugin
 * metadata:
 *   name: plugin-1
 *   labels:
 *     extensions.guqing.xyz/category: attachment
 * spec:
 *   # 'version' is a valid semantic version string (see semver.org).
 *   version: 0.0.1
 *   requires: ">=2.0.0"
 *   author: guqing
 *   logo: example.com/logo.png
 *   pluginClass: xyz.guqing.plugin.potatoes.PotatoesApp
 *   pluginDependencies:
 *    "plugin-2": 1.0.0
 *   # 'homepage' usually links to the GitHub repository of the plugin
 *   homepage: example.com
 *   # 'displayName' explains what the plugin does in only a few words
 *   displayName: "a name to show"
 *   description: "Tell me more about this plugin."
 *   license:
 *     - name: MIT
 * </pre>
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class YamlPluginFinder {
    static final DevelopmentPluginClasspath PLUGIN_CLASSPATH = new DevelopmentPluginClasspath();
    public static final String DEFAULT_PROPERTIES_FILE_NAME = "plugin.yaml";
    private final String propertiesFileName;

    public YamlPluginFinder() {
        this(DEFAULT_PROPERTIES_FILE_NAME);
    }

    public YamlPluginFinder(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    public Plugin find(Path pluginPath) {
        Plugin plugin = readPluginDescriptor(pluginPath);
        if (plugin.getStatus() == null) {
            Plugin.PluginStatus pluginStatus = new Plugin.PluginStatus();
            pluginStatus.setPhase(PluginState.RESOLVED);
            pluginStatus.setLoadLocation(pluginPath.toUri());
            plugin.setStatus(pluginStatus);
        }
        MetadataUtil.nullSafeAnnotations(plugin)
            .put(PluginConst.PLUGIN_PATH, pluginPath.toString());
        return plugin;
    }

    protected Plugin readPluginDescriptor(Path pluginPath) {
        Path propertiesPath = getManifestPath(pluginPath, propertiesFileName);
        try {
            if (propertiesPath == null) {
                throw new PluginRuntimeException("Cannot find the plugin manifest path");
            }

            log.debug("Lookup plugin descriptor in '{}'", propertiesPath);
            if (Files.notExists(propertiesPath)) {
                throw new PluginRuntimeException("Cannot find '{}' path", propertiesPath);
            }
            Resource propertyResource = new FileSystemResource(propertiesPath);
            return unstructuredToPlugin(propertyResource);
        } finally {
            FileUtils.closePath(propertiesPath);
        }
    }

    protected Plugin unstructuredToPlugin(Resource propertyResource) {
        YamlUnstructuredLoader yamlUnstructuredLoader =
            new YamlUnstructuredLoader(propertyResource);
        List<Unstructured> unstructuredList = yamlUnstructuredLoader.load();
        if (unstructuredList.size() != 1) {
            throw new PluginRuntimeException("Unable to find plugin descriptor file '{}'",
                propertiesFileName);
        }
        Unstructured unstructured = unstructuredList.get(0);
        return Unstructured.OBJECT_MAPPER.convertValue(unstructured,
            Plugin.class);
    }

    protected Path getManifestPath(Path pluginPath, String propertiesFileName) {
        if (Files.isDirectory(pluginPath)) {
            for (String location : PLUGIN_CLASSPATH.getClassesDirectories()) {
                var path = pluginPath.resolve(location).resolve(propertiesFileName);
                Resource propertyResource = new FileSystemResource(path);
                if (propertyResource.exists()) {
                    return path;
                }
            }
            throw new PluginRuntimeException(
                "Unable to find plugin descriptor file: " + DEFAULT_PROPERTIES_FILE_NAME);
        } else {
            // it's a jar file
            try {
                return FileUtils.getPath(pluginPath, propertiesFileName);
            } catch (IOException e) {
                throw new PluginRuntimeException(e);
            }
        }
    }
}
