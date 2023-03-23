package run.halo.app.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DevelopmentPluginClasspath;
import org.pf4j.PluginRuntimeException;
import org.pf4j.RuntimeMode;

@Slf4j
public class PluginExtensionLoaderUtils {
    static final String EXTENSION_LOCATION = "extensions";
    static final DevelopmentPluginClasspath PLUGIN_CLASSPATH = new DevelopmentPluginClasspath();

    public static Set<String> lookupExtensions(Path pluginPath, RuntimeMode runtimeMode) {
        if (RuntimeMode.DEVELOPMENT.equals(runtimeMode)) {
            return lookupFromClasses(pluginPath);
        } else {
            return lookupFromJar(pluginPath);
        }
    }

    public static Set<String> lookupFromClasses(Path pluginPath) {
        Set<String> result = new HashSet<>();
        for (String directory : PLUGIN_CLASSPATH.getClassesDirectories()) {
            File file = pluginPath.resolve(directory).resolve(EXTENSION_LOCATION).toFile();
            if (file.exists() && file.isDirectory()) {
                result.addAll(walkExtensionFiles(file.toPath()));
            }
        }
        return result;
    }

    private static Set<String> walkExtensionFiles(Path location) {
        try (Stream<Path> stream = Files.walk(location)) {
            return stream.map(Path::normalize)
                .filter(Files::isRegularFile)
                .filter(path -> isYamlFile(path.getFileName().toString()))
                .map(path -> location.getParent().relativize(path).toString())
                .collect(Collectors.toSet());
        } catch (IOException e) {
            log.debug("Failed to walk extension files from [{}]", location);
            return Collections.emptySet();
        }
    }

    static boolean isYamlFile(String path) {
        return path.endsWith(".yaml") || path.endsWith(".yml");
    }

    /**
     * <p>Lists the path of the unstructured yaml configuration file from the plugin jar.</p>
     *
     * @param pluginJarPath plugin jar path
     * @return Unstructured file paths relative to plugin classpath
     * @throws PluginRuntimeException If loading the file fails
     */
    static Set<String> lookupFromJar(Path pluginJarPath) {
        try (JarFile jarFile = new JarFile(pluginJarPath.toFile())) {
            return jarFile.stream()
                .filter(jarEntry -> {
                    String name = jarEntry.getName();
                    return name.startsWith(EXTENSION_LOCATION)
                        && !jarEntry.isDirectory()
                        && isYamlFile(name);
                })
                .map(ZipEntry::getName)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new PluginRuntimeException(e);
        }
    }
}
