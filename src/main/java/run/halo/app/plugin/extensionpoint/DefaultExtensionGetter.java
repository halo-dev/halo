package run.halo.app.plugin.extensionpoint;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        // TODO Refactoring the way to obtain the definition name according to the class name.
        String definitionName = StringUtils.lowerCase(extensionPoint.getName());
        return client.fetch(ExtensionPointDefinition.class, definitionName)
            .flatMapMany(extensionPointDefinition -> {
                ExtensionPointDefinition.ExtensionPointType type =
                    extensionPointDefinition.getSpec().getType();
                if (type == ExtensionPointDefinition.ExtensionPointType.SINGLETON) {
                    return getEnabledExtension(extensionPoint).flux();
                }
                // TODO If the type is sortable, may need to process the returned order.
                return Flux.fromStream(applicationContext.getBeanProvider(extensionPoint)
                    .orderedStream());
            });
    }
}
