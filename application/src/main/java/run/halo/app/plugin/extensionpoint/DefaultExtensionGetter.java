package run.halo.app.plugin.extensionpoint;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
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

    private final ApplicationContext applicationContext;

    private final ReactiveExtensionClient client;

    @Override
    public <T extends ExtensionPoint> Mono<T> getEnabledExtension(Class<T> extensionPoint) {
        return systemConfigFetcher.fetch(ExtensionPointEnabled.GROUP, ExtensionPointEnabled.class)
            .switchIfEmpty(Mono.just(ExtensionPointEnabled.EMPTY))
            .mapNotNull(enabled -> {
                var implClassNames = enabled.getOrDefault(extensionPoint.getName(), Set.of());
                List<T> allExtensions = getAllExtensions(extensionPoint);
                if (allExtensions.isEmpty()) {
                    return null;
                }
                return allExtensions
                    .stream()
                    .filter(impl -> implClassNames.contains(impl.getClass().getName()))
                    .findFirst()
                    // Fallback to local implementation of the extension point.
                    // This will happen when no proper configuration is found.
                    .orElseGet(() -> allExtensions.get(0));
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

                // TODO If the type is sortable, may need to process the returned order.
                return Flux.fromIterable(getAllExtensions(extensionPoint));
            });
    }

    @Override
    public <T extends ExtensionPoint> Flux<T> getExtensions(Class<T> extensionPointClass) {
        var extensions = new ArrayList<>(pluginManager.getExtensions(extensionPointClass));
        applicationContext.getBeanProvider(extensionPointClass)
            .orderedStream()
            .forEach(extensions::add);
        return Flux.fromIterable(extensions);
    }

    @NonNull
    <T extends ExtensionPoint> List<T> getAllExtensions(Class<T> extensionPoint) {
        Stream<T> pluginExtsStream = pluginManager.getExtensions(extensionPoint)
            .stream();
        Stream<T> systemExtsStream = applicationContext.getBeanProvider(extensionPoint)
            .orderedStream();
        return Stream.concat(systemExtsStream, pluginExtsStream)
            .sorted(new AnnotationAwareOrderComparator())
            .toList();
    }

    Mono<ExtensionPointDefinition> fetchExtensionPointDefinition(
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
