package run.halo.app.plugin.extensionpoint;

import java.util.Set;
import org.pf4j.ExtensionPoint;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting.ExtensionPointEnabled;
import run.halo.app.plugin.HaloPluginManager;

@Component
public class DefaultExtensionGetter implements ExtensionGetter {

    private final SystemConfigurableEnvironmentFetcher systemConfigFetcher;

    private final HaloPluginManager pluginManager;

    private final ApplicationContext applicationContext;

    public DefaultExtensionGetter(SystemConfigurableEnvironmentFetcher systemConfigFetcher,
        HaloPluginManager pluginManager, ApplicationContext applicationContext) {
        this.systemConfigFetcher = systemConfigFetcher;
        this.pluginManager = pluginManager;
        this.applicationContext = applicationContext;
    }

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

}
