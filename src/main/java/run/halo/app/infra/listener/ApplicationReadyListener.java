package run.halo.app.infra.listener;

import java.io.FileNotFoundException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * Scheme ready listener.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    private final HaloProperties haloProperties;
    private final ExtensionClient extensionClient;

    public ApplicationReadyListener(HaloProperties haloProperties,
        ExtensionClient extensionClient) {
        this.haloProperties = haloProperties;
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        initializeExtensions();
    }

    private void initializeExtensions() {
        Set<String> extensionLocations = haloProperties.getInitialExtensionLocations();
        if (!CollectionUtils.isEmpty(extensionLocations)) {

            Resource[] resources = extensionLocations.stream()
                .map(location -> {
                    try {
                        return ResourceUtils.getFile(location);
                    } catch (FileNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .map(FileSystemResource::new)
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
}
