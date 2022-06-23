package run.halo.app.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginState;
import org.pf4j.util.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.PathUtils;
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
 * @since 2.0.0
 */
@Slf4j
public class YamlPluginFinder {
    public static final String DEFAULT_PROPERTIES_FILE_NAME = "plugin.yaml";
    private static final String DEFAULT_RESOURCE_LOCATION = "extensions/";
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
            plugin.setStatus(pluginStatus);
        }
        // read unstructured files
        if (FileUtils.isJarFile(pluginPath)) {
            plugin.getSpec().setExtensionLocations(getUnstructuredFilePathFromJar(pluginPath));
        } else {
            plugin.getSpec().setExtensionLocations(getUnstructuredFileFromClasspath(pluginPath));
        }
        return plugin;
    }

    protected Plugin readPluginDescriptor(Path pluginPath) {
        Path propertiesPath = getManifestPath(pluginPath, propertiesFileName);
        if (propertiesPath == null) {
            throw new PluginRuntimeException("Cannot find the plugin manifest path");
        }

        log.debug("Lookup plugin descriptor in '{}'", propertiesPath);
        if (Files.notExists(propertiesPath)) {
            throw new PluginRuntimeException("Cannot find '{}' path", propertiesPath);
        }
        Resource propertyResource = new FileSystemResource(propertiesPath);
        return unstructuredToPlugin(propertyResource);
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
            for (String location : getSearchLocations()) {
                String s = PathUtils.combinePath(pluginPath.toString(),
                    location, propertiesFileName);
                Path path = Paths.get(s);
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

    /**
     * <p>Lists the path of the unstructured yaml configuration file from the plugin jar.</p>
     *
     * @param jarPath plugin jar path
     * @return Unstructured file paths relative to plugin classpath
     * @throws PluginRuntimeException If loading the file fails
     */
    protected List<String> getUnstructuredFilePathFromJar(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            return jarFile.stream()
                .filter(jarEntry -> {
                    String name = jarEntry.getName();
                    return name.startsWith(DEFAULT_RESOURCE_LOCATION)
                        && !jarEntry.isDirectory()
                        && isYamlFile(name);
                })
                .map(ZipEntry::getName)
                .toList();
        } catch (IOException e) {
            throw new PluginRuntimeException(e);
        }
    }

    private List<String> getUnstructuredFileFromClasspath(Path pluginPath) {
        final Path unstructuredLocation = decisionUnstructuredLocation(pluginPath);
        if (unstructuredLocation == null) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.walk(unstructuredLocation)) {
            return stream.map(Path::normalize)
                .filter(Files::isRegularFile)
                .filter(path -> isYamlFile(path.getFileName().toString()))
                .map(path -> unstructuredLocation.getParent().relativize(path).toString())
                .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Nullable
    private Path decisionUnstructuredLocation(Path pluginPath) {
        for (String searchLocation : getSearchLocations()) {
            String unstructuredLocationString = PathUtils.combinePath(pluginPath.toString(),
                searchLocation, DEFAULT_RESOURCE_LOCATION);
            Path path = Paths.get(unstructuredLocationString);
            boolean exists = Files.exists(path);
            if (exists) {
                return path;
            }
        }
        return null;
    }

    private boolean isYamlFile(String path) {
        return path.endsWith(".yaml") || path.endsWith(".yml");
    }

    private Set<String> getSearchLocations() {
        // TODO 优化路径获取
        return Set.of("build/resources/main/", "target/classes/");
    }
}
