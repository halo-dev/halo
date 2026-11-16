package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DefaultResourceLocalizationService implements ResourceLocalizationService {
    private static final int BATCH_SIZE = 100;
    private static final int MAX_PATCHES = 1000;
    private static final String PERMALINK = "/status/permalink";
    private final ExtensionGetter extensionGetter;
    private final ObjectMapper mapper;

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
                                    patches(subjects, context),
                                    extensionGetter
                                            .getEnabledExtension(ResourceLocalizationProvider.class)
                                            .map(Optional::of)
                                            .defaultIfEmpty(Optional.empty()))
                            .flatMap(tuple -> Flux.fromIterable(sources)
                                    .flatMapSequential(source -> project(
                                            source,
                                            tuple.getT1().get(source.groupVersionKind()),
                                            tuple.getT2(),
                                            tuple.getT3(),
                                            context))
                                    .collectList());
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
            Optional<ResourceLocalizationProvider> provider,
            LocalizationContext context) {
        if (provider.isEmpty()) {
            return Mono.just(source);
        }
        if (descriptor == null) {
            return Mono.just(source);
        }
        if (!descriptor.isEligible(source)) {
            return Mono.just(source);
        }
        return descriptor
                .resolveDocuments(source)
                .defaultIfEmpty(List.of())
                .map(documents -> projectFields(source, descriptor, documents, patches, provider.get(), context));
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
        for (var field : descriptor.fields()) {
            var pointer = field.displayPointer(tree);
            if (pointer == null) {
                continue;
            }
            var node = tree.at(pointer);
            if (isDisplayValue(node)) {
                sources.put(field.key(), node.asText(""));
                pointers.put(field.key(), pointer);
            }
        }
        sources.putAll(ContentSeoFields.extract(source));
        var translated = translations(ResourceReference.from(source), sources, descriptor, documents, patches, context);
        boolean changed = ContentSeoFields.apply(source, tree, translated);
        for (var field : translated.entrySet()) {
            var pointer = pointers.get(field.getKey());
            if (pointer != null) {
                changed |= setDisplayValue(tree, pointer, field.getValue());
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

    private static boolean isDisplayValue(JsonNode node) {
        if (node.isTextual()) {
            return true;
        }
        if (node.isNull()) {
            return true;
        }
        return node.isMissingNode();
    }

    private static boolean setDisplayValue(ObjectNode tree, String pointer, String value) {
        int split = pointer.lastIndexOf('/');
        JsonNode parent = tree.at(pointer.substring(0, split));
        if (!(parent instanceof ObjectNode object)) {
            return false;
        }
        String leaf = pointer.substring(split + 1).replace("~1", "/").replace("~0", "~");
        var previous = object.get(leaf);
        if (previous != null && previous.isTextual() && previous.asText().equals(value)) {
            return false;
        }
        object.put(leaf, value);
        return true;
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
        if (!context.enabled()) {
            return Mono.just(sources);
        }
        return Mono.defer(() -> Mono.zip(descriptors(), patches(List.of(subject), context))
                        .map(tuple -> {
                            Map<String, String> result = new LinkedHashMap<>(sources);
                            result.putAll(translations(
                                    subject,
                                    sources,
                                    tuple.getT1().get(subject.gvk()),
                                    documents,
                                    tuple.getT2(),
                                    context));
                            return result;
                        }))
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(error -> {
                    log.warn("Content localization failed; returning source fields", error);
                    return Mono.just(sources);
                });
    }

    private Mono<Map<GroupVersionKind, ResourceLocalizationDescriptor>> descriptors() {
        return extensionGetter
                .getEnabledExtensions(ResourceLocalizationDescriptor.class)
                .collectMultimap(ResourceLocalizationDescriptor::supports)
                .map(groups -> {
                    var result = new LinkedHashMap<GroupVersionKind, ResourceLocalizationDescriptor>();
                    for (var entry : groups.entrySet()) {
                        if (entry.getValue().size() != 1) {
                            log.warn("Ignoring duplicate localization descriptors for {}", entry.getKey());
                            continue;
                        }
                        var descriptor = entry.getValue().iterator().next();
                        if (descriptor.fields().stream()
                                        .map(LocalizableField::key)
                                        .distinct()
                                        .count()
                                != descriptor.fields().size()) {
                            log.warn(
                                    "Ignoring localization descriptor with duplicate field keys for {}",
                                    entry.getKey());
                            continue;
                        }
                        result.put(entry.getKey(), descriptor);
                    }
                    return result;
                });
    }

    private Mono<List<LocalizationPatch>> patches(List<ResourceReference> subjects, LocalizationContext context) {
        return extensionGetter
                .getEnabledExtension(ResourceLocalizationProvider.class)
                .flatMapMany(provider -> provider.resolveAll(subjects, context))
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

    private Map<String, String> translations(
            ResourceReference subject,
            Map<String, String> sources,
            ResourceLocalizationDescriptor descriptor,
            List<LocalizableDocument> documents,
            List<LocalizationPatch> patches,
            LocalizationContext context) {
        var result = new LinkedHashMap<String, String>();
        if (descriptor == null) {
            return result;
        }
        var locales = new ArrayList<Locale>();
        locales.add(context.targetLocale());
        locales.addAll(context.fallbackLocales());
        var candidates = locales.stream()
                .distinct()
                .takeWhile(locale -> !locale.equals(context.sourceLocale()))
                .map(locale -> findPatch(subject, locale, patches))
                .flatMap(Optional::stream)
                .toList();
        var allowed = descriptor.fields().stream().map(LocalizableField::key).toList();
        var documentFields = new HashSet<String>();
        var declared = new LinkedHashMap<String, List<String>>();
        for (var document : descriptor.documents()) {
            var keys = documentKeys(document);
            declared.put(document.htmlField(), keys);
            documentFields.addAll(keys);
        }
        var resolved = matchingDocuments(documents, declared);
        var sourceHashes = LocalizationHash.sourceFields(sources, resolved);
        for (var document : resolved) {
            var keys = documentKeys(document);
            if (!allowed.containsAll(keys)) {
                continue;
            }
            for (var patch : candidates) {
                var pair = patch.fields().stream()
                        .filter(field -> keys.contains(field.key()))
                        .toList();
                if (pair.size() != keys.size()) {
                    continue;
                }
                if (pair.stream().map(LocalizationPatch.Field::key).distinct().count() != keys.size()) {
                    continue;
                }
                if (pair.stream().allMatch(field -> canApply(field, sourceHashes))) {
                    pair.forEach(field -> result.put(field.key(), field.value()));
                    break;
                }
            }
        }
        for (var key : sourceHashes.keySet()) {
            if (!allowed.contains(key)) {
                continue;
            }
            if (documentFields.contains(key)) {
                continue;
            }
            for (var patch : candidates) {
                var fields = patch.fields().stream()
                        .filter(field -> Objects.equals(field.key(), key))
                        .toList();
                if (fields.size() != 1) {
                    continue;
                }
                var field = fields.getFirst();
                if (canApply(field, sourceHashes)) {
                    result.put(key, field.value());
                    break;
                }
            }
        }
        return result;
    }

    private static List<LocalizableDocument> matchingDocuments(
            List<LocalizableDocument> documents, Map<String, List<String>> declared) {
        var resolved = new ArrayList<LocalizableDocument>();
        var seen = new HashSet<String>();
        for (var document : documents) {
            var key = document.htmlField();
            if (!documentKeys(document).equals(declared.get(key)) || !seen.add(key)) {
                throw new IllegalArgumentException("Resolved document bindings differ from declaration");
            }
            resolved.add(document);
        }
        return resolved;
    }

    private static List<String> documentKeys(LocalizableDocument document) {
        if (document.rawField() == null) {
            return List.of(document.htmlField());
        }
        return List.of(document.htmlField(), document.rawField());
    }

    private static Optional<LocalizationPatch> findPatch(
            ResourceReference subject, Locale locale, List<LocalizationPatch> patches) {
        var matching = patches.stream()
                .filter(patch -> subject.equals(patch.subject()))
                .filter(patch -> locale.equals(patch.locale()))
                .toList();
        // Ambiguous responses must not depend on provider emission order.
        if (matching.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(matching.getFirst());
    }

    private static boolean canApply(LocalizationPatch.Field field, Map<String, String> sourceHashes) {
        if (!sourceHashes.containsKey(field.key())) {
            return false;
        }
        if (field.value() == null) {
            return false;
        }
        if (!sourceHashes.get(field.key()).equals(field.sourceHash())) {
            return false;
        }
        return field.state() == LocalizationPatch.State.APPROVED;
    }
}
