package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * MetadataOperator contains some getters and setters for required fields of metadata.
 *
 * @author johnniang
 */
@JsonDeserialize(as = Metadata.class)
@Schema(implementation = Metadata.class)
public interface MetadataOperator {

    @Schema(required = true)
    @JsonProperty("name")
    String getName();

    @Schema(nullable = true)
    @JsonProperty("labels")
    Map<String, String> getLabels();

    @Schema(nullable = true)
    @JsonProperty("annotations")
    Map<String, String> getAnnotations();

    @Schema(nullable = true)
    @JsonProperty("version")
    Long getVersion();

    @Schema(nullable = true)
    @JsonProperty("creationTimestamp")
    Instant getCreationTimestamp();

    @Schema(nullable = true)
    @JsonProperty("deletionTimestamp")
    Instant getDeletionTimestamp();

    void setName(String name);

    void setLabels(Map<String, String> labels);

    void setAnnotations(Map<String, String> annotations);

    void setVersion(Long version);

    void setCreationTimestamp(Instant creationTimestamp);

    void setDeletionTimestamp(Instant deletionTimestamp);

    static boolean metadataDeepEquals(MetadataOperator left, MetadataOperator right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (!Objects.equals(left.getName(), right.getName())) {
            return false;
        }
        if (!Objects.equals(left.getLabels(), right.getLabels())) {
            return false;
        }
        if (!Objects.equals(left.getAnnotations(), right.getAnnotations())) {
            return false;
        }
        if (!Objects.equals(left.getCreationTimestamp(), right.getCreationTimestamp())) {
            return false;
        }
        if (!Objects.equals(left.getDeletionTimestamp(), right.getDeletionTimestamp())) {
            return false;
        }
        if (!Objects.equals(left.getVersion(), right.getVersion())) {
            return false;
        }
        return true;
    }
}
