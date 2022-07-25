package run.halo.app.infra;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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

    public static final Set<String> REQUIRED_EXTENSION_LOCATIONS =
        Set.of("classpath:/extensions/*.yaml", "classpath:/extensions/*.yml");
    private final HaloProperties haloProperties;
    private final ExtensionClient extensionClient;

    public ExtensionResourceInitializer(HaloProperties haloProperties,
        ExtensionClient extensionClient) {
        this.haloProperties = haloProperties;
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        var locations = new HashSet<String>();
        if (!haloProperties.isRequiredExtensionDisabled()) {
            locations.addAll(REQUIRED_EXTENSION_LOCATIONS);
        }
        if (haloProperties.getInitialExtensionLocations() != null) {
            locations.addAll(haloProperties.getInitialExtensionLocations());
        }

        if (CollectionUtils.isEmpty(locations)) {
            return;
        }

        var resources = locations.stream()
            .map(this::listResources)
            .flatMap(List::stream)
            .distinct()
            .toArray(Resource[]::new);

        log.info("Initializing [{}] extensions in locations: {}", resources.length, locations);

        new YamlUnstructuredLoader(resources).load()
            .forEach(unstructured -> extensionClient.fetch(unstructured.groupVersionKind(),
                    unstructured.getMetadata().getName())
                .ifPresentOrElse(persisted -> {
                    unstructured.getMetadata()
                        .setVersion(persisted.getMetadata().getVersion());
                    // TODO Patch the unstructured instead of update it in the future
                    extensionClient.update(unstructured);
                }, () -> extensionClient.create(unstructured)));

        log.info("Initialized [{}] extensions in locations: {}", resources.length, locations);
    }

    private List<Resource> listResources(String location) {
        var resolver = new PathMatchingResourcePatternResolver();
        try {
            return List.of(resolver.getResources(location));
        } catch (IOException ie) {
            throw new IllegalArgumentException("Invalid extension location: " + location, ie);
        }
    }

}
