package run.halo.app.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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

    @Schema(requiredMode = REQUIRED)
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

    @Schema(requiredMode = REQUIRED)
    @JsonProperty("kind")
    default String getKind() {
        final var gvk = getClass().getAnnotation(GVK.class);
        if (gvk == null) {
            // return null if having no GVK annotation
            return null;
        }
        return gvk.kind();
    }

    @Schema(requiredMode = REQUIRED, implementation = Metadata.class)
    @JsonProperty("metadata")
    MetadataOperator getMetadata();

    void setApiVersion(String apiVersion);

    void setKind(String kind);

    void setMetadata(MetadataOperator metadata);

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
        return ExtensionUtil.isDeleted(extension);
    }
}
