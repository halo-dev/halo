package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.function.Predicate;
import org.springframework.util.StringUtils;

/**
 * ExtensionOperator contains some getters and setters for required fields of Extension.
 *
 * @author johnniang
 */
public interface ExtensionOperator {

    @Schema(required = true)
    @JsonProperty("apiVersion")
    default String getApiVersion() {
        final var gvk = getClass().getAnnotation(GVK.class);
        if (gvk == null) {
            // return null if having no GVK annotation
            return null;
        }
        if (StringUtils.hasText(gvk.group())) {
            return gvk.group() + "/" + gvk.version();
        }
        return gvk.version();
    }

    @Schema(required = true)
    @JsonProperty("kind")
    default String getKind() {
        final var gvk = getClass().getAnnotation(GVK.class);
        if (gvk == null) {
            // return null if having no GVK annotation
            return null;
        }
        return gvk.kind();
    }

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

    static <T extends ExtensionOperator> Predicate<T> isNotDeleted() {
        return ext -> ext.getMetadata().getDeletionTimestamp() == null;
    }

    static boolean isDeleted(ExtensionOperator extension) {
        return extension.getMetadata() != null
            && extension.getMetadata().getDeletionTimestamp() != null;
    }
}
