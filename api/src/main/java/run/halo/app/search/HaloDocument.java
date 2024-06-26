package run.halo.app.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Document for search.
 */
@Data
public final class HaloDocument {

    /**
     * Document ID. It should be unique globally.
     */
    @NotBlank
    private String id;

    /**
     * Metadata name of the corresponding extension.
     */
    @NotBlank
    private String metadataName;

    /**
     * Custom metadata. Make sure the map is serializable.
     */
    private Map<String, String> annotations;

    /**
     * Document title.
     */
    @NotBlank
    private String title;

    /**
     * Document description.
     */
    private String description;

    /**
     * Document content. Safety content, without HTML tag.
     */
    @NotBlank
    private String content;

    /**
     * Document categories. The item in the list is the category metadata name.
     */
    private List<String> categories;

    /**
     * Document tags. The item in the list is the tag metadata name.
     */
    private List<String> tags;

    /**
     * Whether the document is published.
     */
    private boolean published;

    /**
     * Whether the document is recycled.
     */
    private boolean recycled;

    /**
     * Whether the document is exposed to the public.
     */
    private boolean exposed;

    /**
     * Document owner metadata name.
     */
    @NotBlank
    private String ownerName;

    /**
     * Document creation timestamp.
     */
    @PastOrPresent
    private Instant creationTimestamp;

    /**
     * Document update timestamp.
     */
    @PastOrPresent
    private Instant updateTimestamp;

    /**
     * Document permalink.
     */
    @NotBlank
    private String permalink;

    /**
     * Document type. e.g.: post.content.halo.run, singlepage.content.halo.run, moment.moment
     * .halo.run, doc.doc.halo.run.
     */
    @NotBlank
    private String type;

}
