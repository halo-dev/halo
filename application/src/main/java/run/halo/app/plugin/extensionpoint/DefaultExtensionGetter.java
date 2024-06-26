package run.halo.app.plugin.extensionpoint;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting.ExtensionPointEnabled;

@Component
@RequiredArgsConstructor
public class DefaultExtensionGetter implements ExtensionGetter {

    private final SystemConfigurableEnvironmentFetcher systemConfigFetcher;

    private final PluginManager pluginManager;

    private final BeanFactory beanFactory;

    private final ReactiveExtensionClient client;

    @Override
    public <T extends ExtensionPoint> Flux<T> getExtensions(Class<T> extensionPoint) {
        return Flux.fromIterable(pluginManager.getExtensions(extensionPoint))
            .concatWith(
                Flux.fromStream(() -> beanFactory.getBeanProvider(extensionPoint).orderedStream())
            )
            .sort(new AnnotationAwareOrderComparator());
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
                    .concatMap(extensionDefName ->
                        client.fetch(ExtensionDefinition.class, extensionDefName)
                    )
                    .concatMap(extensionDef -> {
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
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(
            equal("spec.className", extensionPoint.getName())
        ));
        var sort = Sort.by("metadata.creationTimestamp", "metadata.name").ascending();
        return client.listBy(ExtensionPointDefinition.class, listOptions,
                PageRequestImpl.ofSize(1).withSort(sort)
            )
            .flatMap(list -> Mono.justOrEmpty(ListResult.first(list)));
    }

}
