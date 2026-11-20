package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * No shared projection cache: fresh source and translation revisions are checked on every batch.
 *
 * @since 2.28.0
 */
@Slf4j
@Component
public class DefaultResourceLocalizationService implements ResourceLocalizationService {
    private static final int BATCH_SIZE = 100;
    private static final int MAX_PATCHES = 1000;
    private static final String PERMALINK = "/status/permalink";
    private final ExtensionGetter extensionGetter;
    private final ObjectMapper mapper;
    private final ResourceLocalizationDescriptors descriptorRegistry;

    @org.springframework.beans.factory.annotation.Autowired
    public DefaultResourceLocalizationService(
            ExtensionGetter extensionGetter, ObjectMapper mapper, ResourceLocalizationDescriptors descriptorRegistry) {
        this.extensionGetter = extensionGetter;
        this.mapper = mapper.copy()
                .configure(com.fasterxml.jackson.databind.cfg.JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false);
        this.descriptorRegistry = descriptorRegistry;
    }

    DefaultResourceLocalizationService(ExtensionGetter extensionGetter, ObjectMapper mapper) {
        this(extensionGetter, mapper, () -> extensionGetter.getEnabledExtensions(ResourceLocalizationDescriptor.class));
    }

    @Override
    public Mono<ConfigMap> localizeSettings(ConfigMap source, LocalizationContext context) {
        if (!context.enabled()) {
            return Mono.just(source);
        }
        if (context.scope() != LocalizationScope.PUBLIC_PAGE && context.scope() != LocalizationScope.PUBLIC_API) {
            return Mono.just(source);
        }
        return Mono.defer(() -> extensionGetter
                        .getEnabledExtension(ResourceLocalizationProvider.class)
                        .flatMap(provider -> Mono.defer(() -> provider.localizeSettings(copySettings(source), context))
                                .contextWrite(reactor -> reactor.delete(LocalizationContext.class)))
                        .map(projected -> settingsData(source, projected)))
                .timeout(Duration.ofSeconds(2))
                .defaultIfEmpty(source)
                .onErrorResume(error -> {
                    log.warn("Settings localization failed; returning source configuration", error);
                    return Mono.just(source);
                });
    }

    private ConfigMap settingsData(ConfigMap source, ConfigMap projected) {
        if (Objects.equals(source.getData(), projected.getData())) {
            return source;
        }
        var copy = copySettings(source);
        copy.setData(projected.getData() == null ? null : new LinkedHashMap<>(projected.getData()));
        return copy;
    }

    private ConfigMap copySettings(ConfigMap source) {
        return mapper.convertValue(source, ConfigMap.class);
    }

    @Override
    public <T extends Extension> Mono<T> localize(T source, LocalizationContext context) {
        return localizeAll(List.of(source), context).map(List::getFirst);
    }

    @Override
    public <T extends Extension> Mono<List<T>> localizeAll(List<T> sources, LocalizationContext context) {
        if (!context.enabled()) {
            return Mono.just(sources);
        }
        if (sources.isEmpty()) {
            return Mono.just(sources);
        }
        return Flux.fromIterable(sources)
                .buffer(BATCH_SIZE)
                .concatMap(batch -> localizeBatch(batch, context))
                .flatMapIterable(items -> items)
                .collectList();
    }

    private <T extends Extension> Mono<List<T>> localizeBatch(List<T> sources, LocalizationContext context) {
        return Mono.defer(() -> {
                    var subjects = sources.stream().map(ResourceReference::from).toList();
                    return Mono.zip(
                                    descriptors(),
                                    extensionGetter
                                            .getEnabledExtension(ResourceLocalizationProvider.class)
                                            .map(Optional::of)
                                            .defaultIfEmpty(Optional.empty()))
                            .flatMap(tuple -> {
                                if (tuple.getT2().isEmpty()) {
                                    return Mono.just(sources);
                                }
                                return patches(subjects, context, tuple.getT2().get())
                                        .flatMap(patches -> Flux.fromIterable(sources)
                                                .flatMapSequential(source -> project(
                                                        source,
                                                        tuple.getT1().get(source.groupVersionKind()),
                                                        patches,
                                                        tuple.getT2().get(),
                                                        context))
                                                .collectList());
                            });
                })
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(error -> {
                    log.warn("Resource localization failed; returning source batch", error);
                    return Mono.just(sources);
                });
    }

    private <T extends Extension> Mono<T> project(
            T source,
            ResourceLocalizationDescriptor descriptor,
            List<LocalizationPatch> patches,
            ResourceLocalizationProvider provider,
            LocalizationContext context) {
        if (descriptor == null) {
            return Mono.just(source);
        }
        if (!descriptor.isEligible(source)) {
            return Mono.just(source);
        }
        return descriptor
                .resolveDocuments(source)
                .defaultIfEmpty(List.of())
                .map(documents -> projectFields(source, descriptor, documents, patches, provider, context));
    }

    private <T extends Extension> T projectFields(
            T source,
            ResourceLocalizationDescriptor descriptor,
            List<LocalizableDocument> documents,
            List<LocalizationPatch> patches,
            ResourceLocalizationProvider provider,
            LocalizationContext context) {
        ObjectNode tree = mapper.valueToTree(source);
        var sources = new LinkedHashMap<String, String>();
        var pointers = new LinkedHashMap<String, String>();
        var fields = descriptor.resolveDisplayFields(tree);
        var schemas = new LinkedHashMap<String, LocalizableSchema>();
        for (var field : fields) {
            var pointer = field.pointer();
            if (pointer == null) {
                continue;
            }
            try {
                sources.put(field.key(), LocalizableValues.encode(field.schema(), tree.at(pointer)));
                pointers.put(field.key(), pointer);
                schemas.put(field.key(), field.schema());
            } catch (IllegalArgumentException ignored) {
                // A source value that does not match its owner's schema is not a translation target.
            }
        }
        var translated = LocalizationProjection.translations(
                ResourceReference.from(source), sources, descriptor.documents(), fields, documents, patches, context);
        boolean changed = false;
        for (var field : translated.entrySet()) {
            var pointer = pointers.get(field.getKey());
            if (pointer != null) {
                var schema = schemas.get(field.getKey());
                try {
                    var value = LocalizableValues.project(
                            schema, tree.at(pointer), LocalizableValues.decode(schema, field.getValue()));
                    changed |= setDisplayValue(tree, pointer, value);
                } catch (IllegalArgumentException ignored) {
                    // Keep the source if the provider supplied an invalid typed replacement.
                }
            }
        }
        changed |= localizeLink(tree, PERMALINK, provider, context);
        if (source.groupVersionKind().equals(GroupVersionKind.fromExtension(MenuItem.class))) {
            changed |= localizeLink(tree, "/status/href", provider, context);
            changed |= localizeLink(tree, "/spec/href", provider, context);
        }
        if (!changed) {
            return source;
        }
        @SuppressWarnings("unchecked")
        T copy = (T) mapper.convertValue(tree, source.getClass());
        return copy;
    }

    private static boolean setDisplayValue(ObjectNode tree, String pointer, String value) {
        return setDisplayValue(tree, pointer, tree.textNode(value));
    }

    private static boolean setDisplayValue(ObjectNode tree, String pointer, JsonNode value) {
        var path = com.fasterxml.jackson.core.JsonPointer.compile(pointer);
        JsonNode parent = tree.at(path.head());
        if (tree.at(path).equals(value)) {
            return false;
        }
        if (parent instanceof ObjectNode object) {
            object.set(path.last().getMatchingProperty(), value);
            return true;
        }
        if (parent instanceof com.fasterxml.jackson.databind.node.ArrayNode array) {
            var index = path.last().getMatchingIndex();
            if (index >= 0 && index < array.size()) {
                array.set(index, value);
                return true;
            }
        }
        return false;
    }

    private boolean localizeLink(
            ObjectNode tree, String pointer, ResourceLocalizationProvider provider, LocalizationContext context) {
        var value = tree.at(pointer);
        if (!value.isTextual()) {
            return false;
        }
        try {
            var url = value.asText();
            var translated = provider.localizeUrl(url, context);
            if (translated != null) {
                return setDisplayValue(tree, pointer, translated);
            }
        } catch (RuntimeException error) {
            log.debug("Unable to localize public URL", error);
        }
        return false;
    }

    @Override
    public Mono<Map<String, String>> localizeFields(
            ResourceReference subject,
            Map<String, String> sources,
            List<LocalizableDocument> documents,
            LocalizationContext context) {
        if (!subject.isExtension()) {
            return Mono.error(
                    new IllegalArgumentException("The descriptor-aware overload requires an Extension reference; "
                            + "use the explicit field-list overload for external resources"));
        }
        if (!context.enabled()) {
            return Mono.just(sources);
        }
        return Mono.defer(() -> descriptors().flatMap(declarations -> {
            var descriptor = declarations.get(subject.gvk());
            if (descriptor == null) {
                return Mono.just(sources);
            }
            return projectSemanticFields(
                    subject, descriptor.fields(), descriptor.documents(), sources, documents, context);
        }));
    }

    @Override
    public Mono<Map<String, String>> localizeFields(
            ResourceReference subject,
            List<LocalizableField> fields,
            Map<String, String> sources,
            List<LocalizableDocument> documents,
            LocalizationContext context) {
        return projectSemanticFields(subject, fields, documents, sources, documents, context);
    }

    private Mono<Map<String, String>> projectSemanticFields(
            ResourceReference subject,
            List<LocalizableField> fields,
            List<LocalizableDocument> declaredDocuments,
            Map<String, String> sources,
            List<LocalizableDocument> documents,
            LocalizationContext context) {
        if (!context.enabled()) {
            return Mono.just(sources);
        }
        return Mono.defer(() -> extensionGetter
                        .getEnabledExtension(ResourceLocalizationProvider.class)
                        .map(Optional::of)
                        .defaultIfEmpty(Optional.empty())
                        .flatMap(optionalProvider -> {
                            if (optionalProvider.isEmpty()) {
                                return Mono.just(sources);
                            }
                            return patches(List.of(subject), context, optionalProvider.get())
                                    .map(patches -> {
                                        Map<String, String> result = new LinkedHashMap<>(sources);
                                        result.putAll(LocalizationProjection.translations(
                                                subject,
                                                sources,
                                                declaredDocuments,
                                                fields,
                                                documents,
                                                patches,
                                                context));
                                        return result;
                                    });
                        }))
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(error -> {
                    log.warn("Content localization failed; returning source fields", error);
                    return Mono.just(sources);
                });
    }

    private Mono<Map<GroupVersionKind, ResourceLocalizationDescriptor>> descriptors() {
        return descriptorRegistry
                .getDescriptors()
                .collectMultimap(ResourceLocalizationDescriptor::supports)
                .map(groups -> {
                    var result = new LinkedHashMap<GroupVersionKind, ResourceLocalizationDescriptor>();
                    groups.forEach((kind, descriptors) ->
                            uniqueDescriptor(kind, descriptors).ifPresent(descriptor -> result.put(kind, descriptor)));
                    return result;
                });
    }

    private static Optional<ResourceLocalizationDescriptor> uniqueDescriptor(
            GroupVersionKind kind, Collection<ResourceLocalizationDescriptor> descriptors) {
        if (descriptors.size() != 1) {
            log.warn("Ignoring duplicate localization descriptors for {}", kind);
            return Optional.empty();
        }
        var descriptor = descriptors.iterator().next();
        var fields = descriptor.fields();
        if (LocalizableFieldResolver.resolveCombined(fields).size() != fields.size()) {
            log.warn("Ignoring localization descriptor with ambiguous field bindings for {}", kind);
            return Optional.empty();
        }
        return Optional.of(descriptor);
    }

    private Mono<List<LocalizationPatch>> patches(
            List<ResourceReference> subjects, LocalizationContext context, ResourceLocalizationProvider provider) {
        return provider.resolveAll(Set.copyOf(subjects), context)
                .contextWrite(reactor -> reactor.delete(LocalizationContext.class))
                .take(MAX_PATCHES + 1L)
                .collectList()
                .timeout(Duration.ofSeconds(2))
                .map(patches -> {
                    if (patches.size() > MAX_PATCHES) {
                        throw new IllegalStateException("Localization provider exceeded batch limit");
                    }
                    return patches;
                });
    }
}
