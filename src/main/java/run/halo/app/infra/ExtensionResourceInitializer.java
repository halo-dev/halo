package run.halo.app.infra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.StringUtils;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * <p>Extension resources initializer.</p>
 * <p>Check whether {@link HaloProperties#getInitialExtensionLocations()} is configured
 * When the system ready, and load resources according to it to creates {@link Unstructured}</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class ExtensionResourceInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final HaloProperties haloProperties;
    private final ExtensionClient extensionClient;

    public ExtensionResourceInitializer(HaloProperties haloProperties,
        ExtensionClient extensionClient) {
        this.haloProperties = haloProperties;
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        Set<String> extensionLocations = haloProperties.getInitialExtensionLocations();
        if (!CollectionUtils.isEmpty(extensionLocations)) {

            Resource[] resources = extensionLocations.stream()
                .map(this::listYamlFiles)
                .flatMap(List::stream)
                .toArray(Resource[]::new);

            log.debug("Initialization loaded [{}] resources to establish.", resources.length);

            new YamlUnstructuredLoader(resources).load()
                .forEach(unstructured -> extensionClient.fetch(unstructured.groupVersionKind(),
                        unstructured.getMetadata().getName())
                    .ifPresentOrElse(persisted -> {
                        unstructured.getMetadata()
                            .setVersion(persisted.getMetadata().getVersion());
                        extensionClient.update(unstructured);
                    }, () -> extensionClient.create(unstructured)));
        }
    }

    private List<FileSystemResource> listYamlFiles(String location) {
        try (Stream<Path> walk = Files.walk(Paths.get(location))) {
            return walk.filter(this::isYamlFile)
                .map(path -> new FileSystemResource(path.toFile()))
                .toList();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private boolean isYamlFile(Path pathname) {
        Path fileName = pathname.getFileName();
        return StringUtils.endsWith(fileName, ".yaml")
            || StringUtils.endsWith(fileName, ".yml");
    }
}
