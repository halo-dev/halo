package run.halo.app.plugin.extensionpoint;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.pf4j.ExtensionPoint;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting.ExtensionPointEnabled;
import run.halo.app.plugin.HaloPluginManager;

@Component
@RequiredArgsConstructor
public class DefaultExtensionGetter implements ExtensionGetter {

    private final SystemConfigurableEnvironmentFetcher systemConfigFetcher;

    private final HaloPluginManager pluginManager;

    private final ApplicationContext applicationContext;

    private final ReactiveExtensionClient client;

    @Override
    public <T extends ExtensionPoint> Mono<T> getEnabledExtension(Class<T> extensionPoint) {
        return systemConfigFetcher.fetch(ExtensionPointEnabled.GROUP, ExtensionPointEnabled.class)
            .switchIfEmpty(Mono.just(ExtensionPointEnabled.EMPTY))
            .mapNotNull(enabled -> {
                var implClassNames = enabled.getOrDefault(extensionPoint.getName(), Set.of());
                return pluginManager.getExtensions(extensionPoint)
                    .stream()
                    .filter(impl -> implClassNames.contains(impl.getClass().getName()))
                    .findFirst()
                    // Fallback to local implementation of the extension point.
                    // This will happen when no proper configuration is found.
                    .orElseGet(() ->
                        applicationContext.getBeanProvider(extensionPoint).getIfAvailable());
            });
    }

    @Override
    public <T extends ExtensionPoint> Flux<T> getEnabledExtensions(Class<T> extensionPoint) {
        return systemConfigFetcher.fetch(ExtensionPointEnabled.GROUP, ExtensionPointEnabled.class)
            .switchIfEmpty(Mono.just(ExtensionPointEnabled.EMPTY))
            .flatMapMany(enabled -> {
                var implClassNames = enabled.getOrDefault(extensionPoint.getName(), Set.of());
                var extensions = pluginManager.getExtensions(extensionPoint)
                    .stream()
                    .filter(impl -> implClassNames.contains(impl.getClass().getName()))
                    .toList();
                if (extensions.isEmpty()) {
                    extensions = applicationContext.getBeanProvider(extensionPoint)
                        .orderedStream()
                        // we only fetch one implementation here
                        .limit(1)
                        .toList();
                }
                return Flux.fromIterable(extensions);
            });
    }

    @Override
    public <T extends ExtensionPoint> Flux<T> getEnabledExtensionByDefinition(
        Class<T> extensionPoint) {
        return fetchExtensionPointDefinition(extensionPoint)
            .flatMapMany(extensionPointDefinition -> {
                ExtensionPointDefinition.ExtensionPointType type =
                    extensionPointDefinition.getSpec().getType();
                if (type == ExtensionPointDefinition.ExtensionPointType.SINGLETON) {
                    return getEnabledExtension(extensionPoint).flux();
                }
                Stream<T> pluginExtsStream = pluginManager.getExtensions(extensionPoint)
                    .stream();
                Stream<T> systemExtsStream = applicationContext.getBeanProvider(extensionPoint)
                    .orderedStream();
                // TODO If the type is sortable, may need to process the returned order.
                return Flux.just(pluginExtsStream, systemExtsStream)
                    .flatMap(Flux::fromStream);
            });
    }

    Mono<ExtensionPointDefinition> fetchExtensionPointDefinition(
        Class<? extends ExtensionPoint> extensionPoint) {
        // TODO Optimize query
        return client.list(ExtensionPointDefinition.class, definition ->
                    extensionPoint.getName().equals(definition.getSpec().getClassName()),
                Comparator.comparing(definition -> definition.getMetadata().getCreationTimestamp())
            )
            .next();
    }
}
