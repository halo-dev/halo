package run.halo.app.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DevelopmentPluginClasspath;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.event.HaloPluginStartedEvent;

/**
 * TODO Optimized Unstructured loading.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginStartedListener implements ApplicationListener<HaloPluginStartedEvent> {

    private final ExtensionClient extensionClient;

    public PluginStartedListener(ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(HaloPluginStartedEvent event) {
        PluginWrapper pluginWrapper = event.getPlugin();
        Plugin plugin =
            extensionClient.fetch(Plugin.class, pluginWrapper.getPluginId()).orElseThrow();
        // load unstructured
        DefaultResourceLoader resourceLoader =
            new DefaultResourceLoader(pluginWrapper.getPluginClassLoader());
        plugin.getSpec().extensionLocationsNonNull().stream()
            .map(resourceLoader::getResource)
            .filter(Resource::exists)
            .map(resource -> new YamlUnstructuredLoader(resource).load())
            .flatMap(List::stream)
            .forEach(unstructured -> {
                MetadataOperator metadata = unstructured.getMetadata();
                Map<String, String> labels = metadata.getLabels();
                if (labels == null) {
                    labels = new HashMap<>();
                    metadata.setLabels(labels);
                }
                labels.put(PluginConst.PLUGIN_NAME_LABEL_NAME, plugin.getMetadata().getName());
                extensionClient.fetch(unstructured.groupVersionKind(), metadata.getName())
                    .ifPresentOrElse(persisted -> {
                        unstructured.getMetadata().setVersion(persisted.getMetadata().getVersion());
                        extensionClient.update(unstructured);
                    }, () -> extensionClient.create(unstructured));
            });
    }

    @Slf4j
    static class PluginExtensionLoaderUtils {
        static final String EXTENSION_LOCATION = "extensions";
        static final DevelopmentPluginClasspath pluginClasspath = new DevelopmentPluginClasspath();

        static Set<String> lookupFromClasses(Path pluginPath) {
            Set<String> result = new HashSet<>();
            for (String directory : pluginClasspath.getClassesDirectories()) {
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
}
