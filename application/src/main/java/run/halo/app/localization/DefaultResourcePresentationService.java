package run.halo.app.localization;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/** Shared, bounded presentation processing for Extension and domain read clients. */
@Slf4j
@Component("resourcePresentationService")
public class DefaultResourcePresentationService implements ResourcePresentationService {
    private static final int BATCH_SIZE = 100;
    private static final int MAX_PATCHES = 1000;
    private final ResourceLocalizationService localization;
    private final ExtensionGetter extensions;

    public DefaultResourcePresentationService(ResourceLocalizationService localization, ExtensionGetter extensions) {
        this.localization = localization;
        this.extensions = extensions;
    }

    @Override
    public <T extends Extension> Mono<List<T>> presentAll(List<T> sources) {
        return publicContext()
                .flatMap(context -> Flux.fromIterable(sources)
                        .buffer(BATCH_SIZE)
                        .concatMap(batch -> localization
                                .localizeAll(batch, context)
                                .flatMap(display -> localizeSettings(display, context)))
                        .flatMapIterable(items -> items)
                        .collectList())
                .defaultIfEmpty(sources);
    }

    @Override
    public <T extends Extension> Flux<T> presentAll(Flux<T> sources) {
        return sources.buffer(BATCH_SIZE).concatMap(this::presentAll).flatMapIterable(items -> items);
    }

    @Override
    public Mono<String> presentUrl(String sourceUrl) {
        return publicContext()
                .filter(LocalizationContext::enabled)
                .flatMap(context -> extensions
                        .getEnabledExtension(ResourceLocalizationProvider.class)
                        .flatMap(provider -> Mono.fromSupplier(() -> provider.localizeUrl(sourceUrl, context))))
                .timeout(Duration.ofSeconds(2))
                .defaultIfEmpty(sourceUrl)
                .onErrorResume(error -> {
                    log.debug("URL presentation failed; returning source URL", error);
                    return Mono.just(sourceUrl);
                });
    }

    private <T extends Extension> Mono<List<T>> localizeSettings(List<T> sources, LocalizationContext context) {
        return Flux.fromIterable(sources)
                .concatMap(source -> {
                    if (source instanceof run.halo.app.extension.ConfigMap settings) {
                        return localization.localizeSettings(settings, context).map(value -> {
                            @SuppressWarnings("unchecked")
                            T result = (T) value;
                            return result;
                        });
                    }
                    return Mono.just(source);
                })
                .collectList();
    }

    @Override
    public <T> Mono<List<T>> presentAll(List<T> sources, LocalizableResourceDescriptor<T> descriptor) {
        return publicContext()
                .flatMap(context -> Flux.fromIterable(sources)
                        .buffer(BATCH_SIZE)
                        .concatMap(batch -> presentBatch(batch, descriptor, context))
                        .flatMapIterable(items -> items)
                        .collectList())
                .defaultIfEmpty(sources);
    }

    private static Mono<LocalizationContext> publicContext() {
        return LocalizationContext.current()
                .filter(context -> context.scope() == LocalizationScope.PUBLIC_PAGE
                        || context.scope() == LocalizationScope.PUBLIC_API);
    }

    private <T> Mono<List<T>> presentBatch(
            List<T> sources, LocalizableResourceDescriptor<T> descriptor, LocalizationContext context) {
        return Mono.defer(() -> {
                    var eligible =
                            sources.stream().filter(descriptor::isEligible).toList();
                    var subjects = eligible.stream()
                            .map(source -> reference(descriptor, source))
                            .toList();
                    return patches(subjects, context)
                            .flatMap(patches -> Flux.fromIterable(sources)
                                    .concatMap(source -> project(source, descriptor, patches, context))
                                    .collectList());
                })
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(error -> {
                    log.warn("Domain resource presentation failed; returning source batch", error);
                    return Mono.just(sources);
                });
    }

    private <T> ResourceReference reference(LocalizableResourceDescriptor<T> descriptor, T source) {
        var reference = descriptor.reference(source);
        if (reference.isExtension()) {
            throw new IllegalArgumentException("Domain resources require an external identity");
        }
        if (!Objects.equals(reference.namespace(), descriptor.namespace())
                || !Objects.equals(reference.type(), descriptor.type())) {
            throw new IllegalArgumentException("Domain identity does not match its descriptor");
        }
        return reference;
    }

    private Mono<List<LocalizationPatch>> patches(List<ResourceReference> subjects, LocalizationContext context) {
        if (!context.enabled() || subjects.isEmpty()) {
            return Mono.just(List.of());
        }
        return extensions
                .getEnabledExtension(ResourceLocalizationProvider.class)
                .flatMapMany(provider -> provider.resolveAll(Set.copyOf(subjects), context)
                        .contextWrite(reactor -> reactor.delete(LocalizationContext.class)))
                .take(MAX_PATCHES + 1L)
                .collectList()
                .map(patches -> {
                    if (patches.size() > MAX_PATCHES) {
                        throw new IllegalStateException("Localization provider exceeded batch limit");
                    }
                    return patches;
                });
    }

    private <T> Mono<T> project(
            T source,
            LocalizableResourceDescriptor<T> descriptor,
            List<LocalizationPatch> patches,
            LocalizationContext context) {
        if (!descriptor.isEligible(source) || !context.enabled()) {
            return Mono.just(source);
        }
        return descriptor
                .resolveDocuments(source)
                .defaultIfEmpty(List.of())
                .map(documents -> projectFields(source, descriptor, documents, patches, context));
    }

    private <T> T projectFields(
            T source,
            LocalizableResourceDescriptor<T> descriptor,
            List<LocalizableDocument> documents,
            List<LocalizationPatch> patches,
            LocalizationContext context) {
        var tree = descriptor.toJson(source);
        validateFields(descriptor.fields());
        var fields = descriptor.resolveDisplayFields(tree);
        var values = sourceValues(tree, fields);
        var translated = LocalizationProjection.translations(
                reference(descriptor, source), values, descriptor.documents(), fields, documents, patches, context);
        if (translated.isEmpty()) {
            return source;
        }
        for (var field : fields) {
            var value = translated.get(field.key());
            if (value == null || field.pointer() == null) {
                continue;
            }
            var pointer = field.pointer();
            var projected = LocalizableValues.project(
                    field.schema(), tree.at(pointer), LocalizableValues.decode(field.schema(), value));
            setValue(tree, pointer, projected);
        }
        var result = descriptor.fromJson(tree);
        if (!descriptor.reference(source).equals(descriptor.reference(result))) {
            throw new IllegalArgumentException("Presentation cannot change a resource identity");
        }
        return result;
    }

    private static void validateFields(List<LocalizableField> fields) {
        if (LocalizableFieldResolver.resolveCombined(fields).size() != fields.size()) {
            throw new IllegalArgumentException("Ambiguous localization field bindings");
        }
    }

    private static Map<String, String> sourceValues(ObjectNode tree, List<LocalizableField> fields) {
        var values = new LinkedHashMap<String, String>();
        for (var field : fields) {
            var pointer = field.pointer();
            if (pointer == null) {
                continue;
            }
            try {
                values.put(field.key(), LocalizableValues.encode(field.schema(), tree.at(pointer)));
            } catch (IllegalArgumentException ignored) {
                // Invalid source values stay source-owned.
            }
        }
        return values;
    }

    private static void setValue(ObjectNode tree, String pointer, JsonNode value) {
        var path = JsonPointer.compile(pointer);
        var parent = tree.at(path.head());
        if (parent instanceof ObjectNode object) {
            object.set(path.last().getMatchingProperty(), value);
        } else if (parent instanceof com.fasterxml.jackson.databind.node.ArrayNode array) {
            var index = path.last().getMatchingIndex();
            if (index >= 0 && index < array.size()) {
                array.set(index, value);
            }
        }
    }
}
