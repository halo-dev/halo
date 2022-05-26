package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ExtensionOperator contains some getters and setters for required fields of Extension.
 *
 * @author johnniang
 */
public interface ExtensionOperator {

    @Schema(required = true)
    @JsonProperty("apiVersion")
    String getApiVersion();

    @Schema(required = true)
    @JsonProperty("kind")
    String getKind();

    @Schema(required = true, implementation = Metadata.class)
    @JsonProperty("metadata")
    MetadataOperator getMetadata();

    void setApiVersion(String apiVersion);

    void setKind(String kind);

    void setMetadata(MetadataOperator metadata);

    /**
     * This method is only for backward compatibility. Same as {@link #getMetadata()}.
     *
     * @return Extension metadata.
     * @see #getMetadata()
     */
    @JsonIgnore
    @Deprecated(forRemoval = true)
    default MetadataOperator metadata() {
        return getMetadata();
    }

    /**
     * This method is only for backward compatibility. Same as
     * {@link #setMetadata(MetadataOperator)}.
     *
     * @param metadata is Extension metadata.
     * @see #setMetadata(MetadataOperator) 
     */
    @Deprecated(forRemoval = true)
    default void metadata(MetadataOperator metadata) {
        setMetadata(metadata);
    }

    /**
     * Sets GroupVersionKind of the Extension.
     *
     * @param gvk is GroupVersionKind data.
     */
    default void groupVersionKind(GroupVersionKind gvk) {
        setApiVersion(gvk.groupVersion().toString());
        setKind(gvk.kind());
    }

    /**
     * Gets GroupVersionKind of the Extension.
     *
     * @return GroupVersionKind of the Extension.
     */
    @JsonIgnore
    default GroupVersionKind groupVersionKind() {
        return GroupVersionKind.fromAPIVersionAndKind(getApiVersion(), getKind());
    }

}
