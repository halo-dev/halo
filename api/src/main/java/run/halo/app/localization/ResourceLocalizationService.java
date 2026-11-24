package run.halo.app.localization;

import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Extension;

/**
 * Creates localized display views of source resources without changing stored content.
 *
 * <p>Consumers must load the source and enforce its read permissions before invoking this service.
 * {@link LocalizationContext} selects a language and consumer scope; it is not an authorization decision. Returned
 * resources, settings and field maps are presentation values and must never be persisted or placed in a shared cache as
 * the original source.
 *
 * <p>Core retains source values when no applicable translation exists. Resource patches must match the declared fields,
 * requested language, approved publication state and current source fingerprints. Disabled contexts leave the source
 * unchanged.
 *
 * <p>For a resource whose read permission has already been checked, use the subscription's context when present and
 * retain the source otherwise:
 *
 * <pre>{@code
 * <T extends Extension> Mono<T> localizePublicResource(T source, ResourceLocalizationService service) {
 *     return LocalizationContext.current()
 *         .flatMap(context -> service.localize(source, context))
 *         .defaultIfEmpty(source);
 * }
 * }</pre>
 *
 * @see LocalizationContext#current()
 * @see ResourceLocalizationDescriptor
 * @since 2.28.0
 */
public interface ResourceLocalizationService {
    /**
     * Projects selected display settings without changing stored or cached configuration.
     *
     * <p>Core delegates to {@link ResourceLocalizationProvider#localizeSettings} only for enabled
     * {@link LocalizationScope#PUBLIC_PAGE} or {@link LocalizationScope#PUBLIC_API} contexts. Search indexing, Console
     * and internal reads retain the source. A projected configuration preserves the source identity; only its data is
     * replaced. Missing providers, empty results or provider failures retain the original configuration.
     *
     * @param source original configuration that must not be modified
     * @param context request language and consumer scope
     * @return isolated display configuration when values change, otherwise the source
     * @since 2.28.0
     */
    default Mono<ConfigMap> localizeSettings(ConfigMap source, LocalizationContext context) {
        return Mono.just(source);
    }

    /**
     * Projects the declared display fields and supported public links of one resource.
     *
     * <p>Core checks descriptor eligibility and projects allowlisted JSON-pointer fields on a copy. It does not load
     * aggregated document content through {@link ResourceLocalizationDescriptor#resolveFields}; use
     * {@code localizeFields} when assembling a view from separately stored content. This method does not perform the
     * caller's read-permission check.
     *
     * @param <T> resource type
     * @param source original, already authorized resource that must not be modified
     * @param context language and consumer scope
     * @return a display copy when values change, otherwise the original resource
     */
    <T extends Extension> Mono<T> localize(T source, LocalizationContext context);

    /**
     * Projects resources in bounded batches while preserving order and cardinality.
     *
     * <p>Use this method for lists instead of calling {@link #localize} for every item. It uses the same projection
     * rules, retains unsupported or ineligible resources, and returns an empty list for empty input. When a batch
     * cannot be projected, Core retains that source batch. The caller must authorize every source resource before
     * passing it here.
     *
     * @param <T> resource type
     * @param sources original resources that must not be modified
     * @param context language and consumer scope
     * @return localized display resources in source order, with one result per input resource
     */
    <T extends Extension> Mono<List<T>> localizeAll(List<T> sources, LocalizationContext context);

    /**
     * Projects non-document semantic fields for a registered Extension resource.
     *
     * <p>This overload supplies no document bindings. Fields declared as part of a document are retained unchanged,
     * even if they appear in {@code sourceFields}. Use the document-aware overload to localize raw content and HTML.
     * The descriptor is resolved from {@code subject.gvk()}. Callers of non-Extension resources must use the overload
     * that accepts an explicit field list. The caller must check resource eligibility and read permissions before
     * resolving source content, because an identity alone cannot establish those properties.
     *
     * @param subject source resource identity
     * @param sourceFields untranslated source values indexed by semantic field key
     * @param context language and consumer scope
     * @return fields with source values retained when no valid translation exists
     * @see #localizeFields(ResourceReference, Map, List, LocalizationContext)
     */
    default Mono<Map<String, String>> localizeFields(
            ResourceReference subject, Map<String, String> sourceFields, LocalizationContext context) {
        return localizeFields(subject, sourceFields, List.of(), context);
    }

    /**
     * Projects non-document semantic fields using an explicit source declaration.
     *
     * <p>Use this overload for non-Extension resources or when the caller already owns the exact display-field
     * allowlist. The subject is an identity only; this method does not resolve a descriptor from it. The caller must
     * check resource eligibility and read permissions before resolving source content.
     *
     * @param subject source resource identity
     * @param fields declared display fields that may be translated
     * @param sourceFields untranslated source values indexed by semantic field key
     * @param context language and consumer scope
     * @return fields with source values retained when no valid translation exists
     */
    default Mono<Map<String, String>> localizeFields(
            ResourceReference subject,
            List<LocalizableField> fields,
            Map<String, String> sourceFields,
            LocalizationContext context) {
        return localizeFields(subject, fields, sourceFields, List.of(), context);
    }

    /**
     * Projects semantic fields and complete documents for a registered Extension resource.
     *
     * <p>Pass the original field values and resolved document bindings from the same source revision. Bindings must
     * match the descriptor's declared HTML/raw key pairs; their formats may be resolved per resource. Core applies a
     * document only when all its parts have valid, approved translations in the same candidate language. It retains
     * incomplete, stale or omitted documents unchanged.
     *
     * <p>For a descriptor declaring {@code content} and {@code raw} as a Markdown document:
     *
     * <pre>{@code
     * var fields = Map.of("content", "<p>Hello</p>", "raw", "Hello");
     * var documents = List.of(new LocalizableDocument("content", "raw", "markdown", null));
     * Mono<Map<String, String>> localized = service.localizeFields(
     *     ResourceReference.from(source), fields, documents, context);
     * }</pre>
     *
     * <p>The result contains display values only. It neither renders Markdown nor runs content processors; the consumer
     * continues its normal rendering pipeline with the selected values. Callers must enforce resource eligibility and
     * read permissions before resolving content.
     *
     * @param subject source resource identity
     * @param sourceFields untranslated values indexed by semantic key, including all parts of each document to be
     *     projected
     * @param documents document bindings resolved for this source revision; omitted bindings are not projected
     * @param context language and consumer scope
     * @return fields with valid translations applied and remaining source values retained
     */
    Mono<Map<String, String>> localizeFields(
            ResourceReference subject,
            Map<String, String> sourceFields,
            List<LocalizableDocument> documents,
            LocalizationContext context);

    /**
     * Projects semantic fields and complete documents using an explicit source declaration.
     *
     * <p>The supplied documents define the permitted HTML/raw pairs for this read. Use this overload for non-Extension
     * resources or when the caller already owns the exact display-field and document declarations. The subject is an
     * identity only. The caller must enforce source eligibility and read permissions.
     *
     * @param subject source resource identity
     * @param fields declared display fields that may be translated
     * @param sourceFields untranslated source values indexed by semantic field key
     * @param documents source document bindings for this resource
     * @param context language and consumer scope
     * @return fields with valid translations applied and remaining source values retained
     */
    Mono<Map<String, String>> localizeFields(
            ResourceReference subject,
            List<LocalizableField> fields,
            Map<String, String> sourceFields,
            List<LocalizableDocument> documents,
            LocalizationContext context);
}
