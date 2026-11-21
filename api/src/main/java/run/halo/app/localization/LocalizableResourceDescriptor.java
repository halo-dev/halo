package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Source-owned declaration for a display DTO whose data is stored outside Extension storage.
 *
 * <p>The owning plugin supplies its normal database read adapter. No synthetic Extension, GVK or database record is
 * created. Field declarations use the same schemas and projection rules as Extension descriptors. Return a detached
 * DTO, never a managed persistence entity; projections must not be saved through the original repository.
 *
 * @param <T> detached display DTO type
 * @since 2.28.0
 */
public interface LocalizableResourceDescriptor<T> {
    /** Stable owner namespace, including tenancy when necessary for identity uniqueness. */
    String namespace();

    /** Stable resource type within the namespace. */
    String type();

    /** Concrete Jackson-serializable display type. */
    Class<T> resourceClass();

    /**
     * Serializes a detached source with the property model used by this declaration. Catalog reads, identity resolution
     * and presentation processing must use the same shape. Override both codec methods when the source uses custom
     * Jackson configuration; the annotation factory captures its supplied mapper automatically.
     *
     * @return an independently mutable object tree, never a shared source tree
     */
    default ObjectNode toJson(T source) {
        return LocalizableResourceDescriptors.toJson(source);
    }

    /** Reconstructs a detached display DTO using the same property model as {@link #toJson(Object)}. */
    default T fromJson(ObjectNode source) {
        return LocalizableResourceDescriptors.fromJson(source, resourceClass());
    }

    /** Stable source identity; never derived from a translated title or URL. */
    ResourceReference reference(T source);

    /** Explicit display allowlist, using stable translation keys and source JSON pointers. */
    List<LocalizableField> fields();

    /** Source-owned dynamic display collections using stable entry identities. */
    default List<LocalizableFieldCollection> fieldCollections() {
        return List.of();
    }

    /** Resolves static and dynamic fields against this source, using the same collision rules as Extensions. */
    default List<LocalizableField> resolveDisplayFields(JsonNode source) {
        return LocalizableFieldResolver.resolve(fields(), fieldCollections(), source);
    }

    /** Optional document pairs; use the same source revision for raw and rendered values. */
    default List<LocalizableDocument> documents() {
        return List.of();
    }

    /**
     * Resolves format and editor metadata for this source revision, without changing the declared HTML/raw field pairs.
     * Override when records use different editors, such as HTML and Markdown in the same resource type. The source
     * adapter must supply raw and rendered content from the same revision.
     */
    default Mono<List<LocalizableDocument>> resolveDocuments(T source) {
        return Mono.just(documents());
    }

    /** Catalog label message code or literal, resolved through the owning plugin message bundle. */
    default String displayName() {
        return type();
    }

    /** Declared semantic key used as the catalog title, or null to use the source ID. */
    default String titleField() {
        return null;
    }

    /** Declared semantic key used as the catalog summary, or null when no summary is supplied. */
    default String summaryField() {
        return null;
    }

    /** Business eligibility, not a substitute for the source adapter's authorization check. */
    default boolean isEligible(T source) {
        return true;
    }
}
