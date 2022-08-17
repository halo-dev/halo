package run.halo.app.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DevelopmentPluginClasspath;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.event.HaloPluginStartedEvent;

/**
 * TODO Optimized Unstructured loading.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginStartedListener {

    private final ReactiveExtensionClient client;

    public PluginStartedListener(ReactiveExtensionClient extensionClient) {
        this.client = extensionClient;
    }

    @EventListener
    public Mono<Void> onApplicationEvent(HaloPluginStartedEvent event) {
        PluginWrapper pluginWrapper = event.getPlugin();
        var resourceLoader =
            new DefaultResourceLoader(pluginWrapper.getPluginClassLoader());
        var pluginApplicationContext = ExtensionContextRegistry.getInstance()
            .getByPluginId(pluginWrapper.getPluginId());
        return client.get(Plugin.class, pluginWrapper.getPluginId())
            .zipWith(Mono.just(
                lookupExtensions(pluginWrapper.getPluginPath(), pluginWrapper.getRuntimeMode())))
            .flatMap(tuple2 -> {
                var plugin = tuple2.getT1();
                var extensionLocations = tuple2.getT2();
                return Flux.fromIterable(extensionLocations)
                    .map(resourceLoader::getResource)
                    .filter(Resource::exists)
                    .map(resource -> new YamlUnstructuredLoader(resource).load())
                    .flatMapIterable(rs -> rs)
                    .flatMap(unstructured -> {
                        var metadata = unstructured.getMetadata();
                        // collector plugin initialize extension resources
                        pluginApplicationContext.addExtensionMapping(
                            unstructured.groupVersionKind(),
                            metadata.getName());
                        var labels = metadata.getLabels();
                        if (labels == null) {
                            labels = new HashMap<>();
                        }
                        labels.put(PluginConst.PLUGIN_NAME_LABEL_NAME,
                            plugin.getMetadata().getName());
                        metadata.setLabels(labels);

                        return client.fetch(unstructured.groupVersionKind(), metadata.getName())
                            .flatMap(extension -> {
                                unstructured.getMetadata()
                                    .setVersion(extension.getMetadata().getVersion());
                                return client.update(unstructured);
                            })
                            .switchIfEmpty(Mono.defer(() -> client.create(unstructured)));
                    }).then();
            }).then();
    }

    Set<String> lookupExtensions(Path pluginPath, RuntimeMode runtimeMode) {
        if (RuntimeMode.DEVELOPMENT.equals(runtimeMode)) {
            return PluginExtensionLoaderUtils.lookupFromClasses(pluginPath);
        } else {
            return PluginExtensionLoaderUtils.lookupFromJar(pluginPath);
        }
    }

    @Slf4j
    static class PluginExtensionLoaderUtils {
        static final String EXTENSION_LOCATION = "extensions";
        static final DevelopmentPluginClasspath PLUGIN_CLASSPATH = new DevelopmentPluginClasspath();

        static Set<String> lookupFromClasses(Path pluginPath) {
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
}
