package run.halo.app.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import run.halo.app.localization.ResourceReference;

/** Document for search. */
@Data
public final class HaloDocument {

    /** Document ID. It should be unique globally. */
    @NotBlank
    private String id;

    /**
     * Content language of a localized search variant, expressed as a BCP 47 tag such as {@code en} or {@code zh-Hant}.
     * A null value identifies an original document, regardless of the language in which its content was written.
     *
     * <p>Core sets this field when expanding {@link HaloDocumentVariantProvider.Variant} values. Locale-aware search
     * engines use it to isolate translated results from original-document searches. It does not change resource
     * ownership or visibility.
     *
     * @since 2.28.0
     */
    private String locale;

    /**
     * Index ID of the original document from which this language variant was derived, or null for an original document.
     * It refers to the original document's {@link #id}, not its resource's {@link #metadataName}.
     *
     * <p>Core assigns this relationship during variant expansion. Search engines that support localized documents
     * remove existing variants referencing this ID when the source is replaced or deleted, preventing stale
     * translations from remaining searchable.
     *
     * @since 2.28.0
     */
    private String sourceId;

    /**
     * Stable metadata name of the corresponding Extension, or the owner-defined stable identifier for a non-Extension
     * source. This value is never a translated title or URL.
     */
    @NotBlank
    private String metadataName;

    /**
     * Stable resource identity of the indexed source, or null when the provider does not supply one.
     *
     * <p>Localized variant providers use this value to resolve translations without inferring a resource type from
     * {@link #type}. Providers for Extension resources should set it from the source extension; providers for
     * non-Extension data should use {@link ResourceReference#external(String, String, String)}. A search engine may
     * omit it when returning results because variant expansion happens before indexing.
     *
     * @since 2.28.0
     */
    @Nullable
    private ResourceReference resource;

    /** Custom metadata. Make sure the map is serializable. */
    private Map<String, String> annotations;

    /** Document title. */
    @NotBlank
    private String title;

    /** Document description. */
    private String description;

    /** Document content. Safety content, without HTML tag. */
    @NotBlank
    private String content;

    /** Document categories. The item in the list is the category metadata name. */
    private List<String> categories;

    /** Document tags. The item in the list is the tag metadata name. */
    private List<String> tags;

    /** Whether the document is published. */
    private boolean published;

    /** Whether the document is recycled. */
    private boolean recycled;

    /** Whether the document is exposed to the public. */
    private boolean exposed;

    /** Document owner metadata name. */
    @NotBlank
    private String ownerName;

    /** Document creation timestamp. */
    @PastOrPresent
    private Instant creationTimestamp;

    /** Document update timestamp. */
    @PastOrPresent
    private Instant updateTimestamp;

    /** Document permalink. */
    @NotBlank
    private String permalink;

    /**
     * Document type. e.g.: post.content.halo.run, singlepage.content.halo.run, moment.moment.halo.run,
     * doc.doc.halo.run.
     */
    @NotBlank
    private String type;
}
