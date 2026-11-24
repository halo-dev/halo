package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Describes the display fields and editable documents of a resource owned by Core or a plugin.
 *
 * <p>This is a {@code MULTI_INSTANCE} extension point: each resource type has its own descriptor. Core ignores a
 * resource type when multiple enabled descriptors claim the same {@link GroupVersionKind}, or when its descriptor
 * declares duplicate keys or overlapping display pointers. A descriptor defines what may be translated; it neither
 * stores translations nor grants access to the resource.
 *
 * <p>For example, a plugin resource with title and summary fields can declare:
 *
 * <pre>{@code
 * var descriptor = new ResourceLocalizationDescriptor() {
 *     @Override
 *     public GroupVersionKind supports() {
 *         return new GroupVersionKind("example.com", "v1alpha1", "Notice");
 *     }
 *
 *     @Override
 *     public List<LocalizableField> fields() {
 *         return List.of(
 *             new LocalizableField("title", "/spec/title", LocalizableField.Format.PLAIN_TEXT),
 *             new LocalizableField("summary", "/spec/summary", LocalizableField.Format.PLAIN_TEXT));
 *     }
 * };
 * }</pre>
 *
 * <p>Plugin implementations must also register with Halo's extension-point mechanism. Semantic keys such as
 * {@code title} identify stored translations, so they should remain stable when the resource's storage layout changes.
 *
 * @see ResourceLocalizationProvider
 * @see ResourceLocalizationService
 * @since 2.28.0
 */
public interface ResourceLocalizationDescriptor extends ExtensionPoint {
    /**
     * Gets the exact resource type owned by this descriptor.
     *
     * <p>The version is part of the identity. A descriptor for one API version does not implicitly cover other versions
     * of the same kind.
     *
     * @return the non-null supported group, version and kind
     */
    GroupVersionKind supports();

    /**
     * Gets the allowlist of display fields that may be translated.
     *
     * <p>Declare only display values, not resource identities, permissions or other operational settings. A field with
     * a JSON pointer is extracted from that display leaf. A field with a null pointer is supplied through
     * {@link #resolveFields(Extension, Map)}, for example when document content lives in a separate snapshot.
     *
     * @return a non-null list of fields with unique, stable semantic keys
     */
    List<LocalizableField> fields();

    /**
     * Gets source-owned collections containing dynamically identified display fields.
     *
     * <p>Namespaces must be unique. Declare only the permitted value property, never arbitrary paths supplied by a
     * translation provider. The default declares no collections.
     *
     * @return collection declarations, each with a distinct namespace
     * @see LocalizableFieldCollection
     */
    default List<LocalizableFieldCollection> fieldCollections() {
        return List.of();
    }

    /**
     * Expands this resource's display fields using its current collection identities and positions.
     *
     * <p>Both source extraction and display projection must use this method for collections. Static fields retain
     * precedence for identical pointers; dynamic bindings with duplicate namespaces, keys or effective pointers are
     * omitted. Ancestor/descendant pointer conflicts are excluded, so separate translation units cannot overwrite each
     * other's object or array members. No field discovered on another resource is implicitly authorized on this
     * resource.
     *
     * @param source original resource JSON, never a translated copy
     * @return immutable list containing static fields and unambiguous dynamic fields
     */
    default List<LocalizableField> resolveDisplayFields(JsonNode source) {
        return LocalizableFieldResolver.resolve(fields(), fieldCollections(), source);
    }

    /**
     * Gets the resource label message code displayed in the translation catalog.
     *
     * <p>Core resolves the value through the owning Core or plugin message bundle and keeps the declared value as a
     * literal fallback.
     *
     * @return a label message code or literal, defaulting to the resource kind
     */
    default String displayName() {
        return supports().kind();
    }

    /**
     * Identifies the declared field used as the resource's display title. The key is opaque; consumers must not infer a
     * title field from property names. A null value lets a consumer use the resource's existing display identity.
     *
     * @return a declared field key, or null when the resource does not supply a display title
     */
    default String titleField() {
        return null;
    }

    /**
     * Identifies the declared field used as the resource's summary or description.
     *
     * <p>The key is opaque; consumers must not infer a summary field from property names. A null value lets a consumer
     * retain its existing summary value.
     *
     * @return a declared field key, or null when the resource does not supply a summary
     */
    default String summaryField() {
        return null;
    }

    /**
     * Gets JSON pointers to source-owned textual values that contain public URLs.
     *
     * <p>A URL is not a translation unit and must not be declared through {@link #fields()}. Core passes each value to
     * {@link ResourceLocalizationProvider#localizeUrl(String, LocalizationContext)} when it projects a resource in a
     * language context. Invalid, duplicate or display-field-overlapping pointers and non-textual values are ignored.
     * The default declares no links.
     *
     * @return JSON pointers to public URL values; each pointer must be an absolute non-root JSON pointer
     */
    default List<String> linkPointers() {
        return List.of();
    }

    /**
     * Gets the independent editable documents contained in this resource.
     *
     * <p>Each binding identifies rendered HTML and, when present, its raw source. Both keys must be declared by
     * {@link #fields()}. Core applies a raw/HTML pair together from the same language and matching source revision; it
     * does not combine independently translated halves. The default implementation declares no documents.
     *
     * @return a non-null list of document bindings using declared semantic keys
     * @see LocalizableDocument
     */
    default List<LocalizableDocument> documents() {
        return List.of();
    }

    /**
     * Resolves editor metadata for a particular resource.
     *
     * <p>Returned bindings must use the same HTML/raw key pairs as {@link #documents()}; only the raw type and editor
     * may vary, for example when a released snapshot selects a Markdown editor. Omitted document bindings are not
     * projected. Resolve the metadata from the same source revision as the field values, without changing the source
     * resource or blocking.
     *
     * @param resource source resource of the supported type that must not be modified
     * @return a publisher of the resource-specific document bindings, defaulting to {@link #documents()}
     */
    default Mono<List<LocalizableDocument>> resolveDocuments(Extension resource) {
        return Mono.just(documents());
    }

    /**
     * Determines whether a resource may participate in translation.
     *
     * <p>Used by the translation catalog, automation and resource projection to exclude unsuitable resources, such as
     * recycled content. This check is not authorization: callers must still enforce read permissions before loading or
     * exposing content. Keep it non-blocking and free of side effects.
     *
     * @param resource source resource of the supported type to inspect
     * @return true if the resource is eligible; the default implementation accepts all resources
     */
    default boolean isEligible(Extension resource) {
        return true;
    }

    /**
     * Determines whether this resource is publicly readable and visible in a public listing.
     *
     * <p>This is a resource-owner declaration, not a permission check for the current request. It is used by consumers
     * that build public discovery surfaces such as sitemaps and alternate-language links. The default is {@code false},
     * so an unknown Extension is not exposed merely because it has a localization descriptor. Implementations must keep
     * this check non-blocking and side-effect free.
     *
     * @param resource source resource of the supported type
     * @return true when the source has a public, published representation
     */
    default boolean publiclyVisible(Extension resource) {
        return false;
    }

    /**
     * Determines whether this public resource may appear in indexes and sitemaps.
     *
     * <p>This is separate from {@link #publiclyVisible(Extension)} because a resource can be publicly readable while
     * explicitly asking search engines not to index it. The default follows the public visibility decision.
     *
     * @param resource source resource of the supported type
     * @return true when public discovery surfaces may include the source
     */
    default boolean indexable(Extension resource) {
        return publiclyVisible(resource);
    }

    /**
     * Gets the source-owned public URL used by localization consumers.
     *
     * <p>The returned value is the original source URL, not a translated or language-prefixed URL. It may be empty when
     * the resource has no public page. Callers remain responsible for the source's public read authorization.
     *
     * @param resource source resource of the supported type
     * @return the original public URL, or an empty string
     */
    default String permalink(Extension resource) {
        return "";
    }

    /**
     * Gets the source revision used for catalog and translation freshness checks.
     *
     * <p>This value belongs to the resource storage format, not to a translated value. The default is empty.
     *
     * @param resource source resource of the supported type
     * @return a source revision, or an empty string when the format has none
     */
    default String revision(Extension resource) {
        return "";
    }

    /**
     * Gets the source update time used for catalog ordering and display.
     *
     * @param resource source resource of the supported type
     * @return the source update time, or null when unavailable
     */
    default Instant updatedAt(Extension resource) {
        var metadata = resource.getMetadata();
        return metadata == null ? null : metadata.getCreationTimestamp();
    }

    /**
     * Resolves untranslated semantic field values, including content stored in released snapshots.
     *
     * <p>Start with the extracted display fields and add content declared with a null pointer. Return only keys from
     * {@link #resolveDisplayFields(JsonNode)}, and obtain document values and editor metadata from the same source
     * revision. Implementations should return a new map when enriching the input, perform reactive reads without
     * blocking, and never write or translate the source. The default implementation returns the extracted map
     * unchanged.
     *
     * <p>This hook is used by callers that collect source content; projecting a resource through
     * {@link ResourceLocalizationService#localize(Extension, LocalizationContext)} does not automatically load its
     * aggregated documents. Use the service's document-aware {@code localizeFields} overload when constructing such a
     * content view.
     *
     * @param resource source resource of the supported type that must not be modified
     * @param extracted display fields already extracted from declared JSON pointers
     * @return a publisher of source values indexed only by declared semantic field keys
     */
    default Mono<Map<String, String>> resolveFields(Extension resource, Map<String, String> extracted) {
        return Mono.just(extracted);
    }
}
