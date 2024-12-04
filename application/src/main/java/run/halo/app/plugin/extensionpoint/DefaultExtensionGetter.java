package run.halo.app.plugin.extensionpoint;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting.ExtensionPointEnabled;

@Component
@RequiredArgsConstructor
public class DefaultExtensionGetter implements ExtensionGetter {

    private final SystemConfigurableEnvironmentFetcher systemConfigFetcher;

    private final PluginManager pluginManager;

    private final BeanFactory beanFactory;

    private final ExtensionDefinitionGetter extensionDefinitionGetter;

    private final ExtensionPointDefinitionGetter extensionPointDefinitionGetter;

    @Override
    public <T extends ExtensionPoint> Flux<T> getExtensions(Class<T> extensionPoint) {
        return Flux.fromIterable(pluginManager.getExtensions(extensionPoint))
            .concatWith(
                Flux.fromStream(() -> beanFactory.getBeanProvider(extensionPoint).orderedStream())
            )
            .sort(new AnnotationAwareOrderComparator());
    }

    @Override
    public <T extends ExtensionPoint> List<T> getExtensionList(Class<T> extensionPoint) {
        var extensions = new LinkedList<T>();
        Optional.ofNullable(pluginManager.getExtensions(extensionPoint))
            .ifPresent(extensions::addAll);
        extensions.addAll(beanFactory.getBeanProvider(extensionPoint).orderedStream().toList());
        extensions.sort(new AnnotationAwareOrderComparator());
        return extensions;
    }

    @Override
    public <T extends ExtensionPoint> Mono<T> getEnabledExtension(Class<T> extensionPoint) {
        return getEnabledExtensions(extensionPoint).next();
    }

    @Override
    public <T extends ExtensionPoint> Flux<T> getEnabledExtensions(
        Class<T> extensionPoint) {
        return fetchExtensionPointDefinition(extensionPoint)
            .flatMapMany(epd -> {
                var epdName = epd.getMetadata().getName();
                var type = epd.getSpec().getType();
                if (type == ExtensionPointDefinition.ExtensionPointType.SINGLETON) {
                    return getEnabledExtensions(epdName, extensionPoint).take(1);
                }
                // TODO If the type is sortable, may need to process the returned order.
                return getEnabledExtensions(epdName, extensionPoint);
            });
    }

    private <T extends ExtensionPoint> Flux<T> getEnabledExtensions(String epdName,
        Class<T> extensionPoint) {
        return systemConfigFetcher.fetch(ExtensionPointEnabled.GROUP, ExtensionPointEnabled.class)
            .switchIfEmpty(Mono.fromSupplier(ExtensionPointEnabled::new))
            .flatMapMany(enabled -> {
                var extensionDefNames = enabled.getOrDefault(epdName, null);
                if (extensionDefNames == null) {
                    // get all extensions if not specified
                    return Flux.defer(() -> getExtensions(extensionPoint));
                }
                var extensions = getExtensions(extensionPoint).cache();
                return Flux.fromIterable(extensionDefNames)
                    .flatMapSequential(extensionDefinitionGetter::get)
                    .flatMapSequential(extensionDef -> {
                        var className = extensionDef.getSpec().getClassName();
                        return extensions.filter(
                            extension -> Objects.equals(extension.getClass().getName(),
                                className)
                        );
                    });
            });
    }

    private Mono<ExtensionPointDefinition> fetchExtensionPointDefinition(
        Class<? extends ExtensionPoint> extensionPoint) {
        return extensionPointDefinitionGetter.getByClassName(extensionPoint.getName());
    }

}
