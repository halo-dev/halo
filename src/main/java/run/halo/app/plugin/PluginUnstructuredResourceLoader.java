package run.halo.app.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * Plug in unstructured data loader.
 * TODO Rename this class to an appropriate name.
 *
 * @author guqing
 * @see YamlUnstructuredLoader
 * @see PluginWrapper
 * @see DefaultResourceLoader
 * @since 2.0.0
 */
public class PluginUnstructuredResourceLoader {
    private static final String DEFAULT_RESOURCE_LOCATION = "extensions/";
    private final String resourceLocation;

    public PluginUnstructuredResourceLoader() {
        resourceLocation = DEFAULT_RESOURCE_LOCATION;
    }

    public PluginUnstructuredResourceLoader(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    /**
     * Loading unstructured yaml configuration files in plugins.
     *
     * @param pluginWrapper Wrapper object holding plugin data
     * @return a collection of {@link Unstructured} data(never null)
     */
    @NonNull
    public List<Unstructured> loadUnstructured(PluginWrapper pluginWrapper) {
        List<String> unstructuredFilePaths =
            getUnstructuredFilePathFromJar(pluginWrapper.getPluginPath());

        DefaultResourceLoader resourceLoader =
            new DefaultResourceLoader(pluginWrapper.getPluginClassLoader());
        Resource[] resources = unstructuredFilePaths.stream()
            .map(resourceLoader::getResource)
            .filter(Resource::exists)
            .toArray(Resource[]::new);

        YamlUnstructuredLoader yamlUnstructuredLoader = new YamlUnstructuredLoader(resources);
        return yamlUnstructuredLoader.load();
    }

    /**
     * <p>Lists the path of the unstructured yaml configuration file from the plugin jar.</p>
     *
     * @param jarPath plugin jar path
     * @return Unstructured file paths relative to plugin classpath
     * @throws PluginRuntimeException If loading the file fails
     */
    public List<String> getUnstructuredFilePathFromJar(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            return jarFile.stream()
                .filter(jarEntry -> {
                    String name = jarEntry.getName();
                    return name.startsWith(resourceLocation)
                        && !jarEntry.isDirectory()
                        && isYamlFile(name);
                })
                .map(ZipEntry::getName)
                .toList();
        } catch (IOException e) {
            throw new PluginRuntimeException(e);
        }
    }

    private boolean isYamlFile(String path) {
        return path.endsWith(".yaml") || path.endsWith(".yml");
    }
}
