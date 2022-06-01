package run.halo.app.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginRuntimeException;
import org.pf4j.util.FileUtils;
import org.springframework.core.io.FileSystemResource;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.JsonUtils;
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
 *   license: MIT
 * </pre>
 *
 * @author guqing
 * @see DefaultPluginDescriptor
 * @since 2.0.0
 */
@Slf4j
public class YamlPluginDescriptorFinder implements PluginDescriptorFinder {
    public static final String DEFAULT_PROPERTIES_FILE_NAME = "plugin.yaml";

    protected String propertiesFileName;

    public YamlPluginDescriptorFinder() {
        this(DEFAULT_PROPERTIES_FILE_NAME);
    }

    public YamlPluginDescriptorFinder(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath)
            && (Files.isDirectory(pluginPath)
            || FileUtils.isJarFile(pluginPath));
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        return readPluginDescriptor(pluginPath);
    }

    protected DefaultPluginDescriptor readPluginDescriptor(Path pluginPath) {
        Path propertiesPath = getManifestPath(pluginPath, propertiesFileName);
        if (propertiesPath == null) {
            throw new PluginRuntimeException("Cannot find the plugin manifest path");
        }

        log.debug("Lookup plugin descriptor in '{}'", propertiesPath);
        if (Files.notExists(propertiesPath)) {
            throw new PluginRuntimeException("Cannot find '{}' path", propertiesPath);
        }
        YamlUnstructuredLoader yamlUnstructuredLoader =
            new YamlUnstructuredLoader(new FileSystemResource(propertiesPath));
        List<Unstructured> unstructuredList = yamlUnstructuredLoader.load();
        if (unstructuredList.size() != 1) {
            throw new PluginRuntimeException("Unable to find plugin descriptor file '{}'",
                propertiesFileName);
        }
        Unstructured unstructured = unstructuredList.get(0);
        try {
            return JsonUtils.jsonToObject(JsonUtils.objectToJson(unstructured),
                DefaultPluginDescriptor.class);
        } catch (JsonProcessingException e) {
            throw new PluginRuntimeException(e);
        }
    }

    protected Path getManifestPath(Path pluginPath, String propertiesFileName) {
        if (Files.isDirectory(pluginPath)) {
            return pluginPath.resolve(Paths.get(propertiesFileName));
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
