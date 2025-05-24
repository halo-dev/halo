package run.halo.app.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * MetadataOperator contains some getters and setters for required fields of metadata.
 *
 * @author johnniang
 */
@JsonDeserialize(as = Metadata.class)
@Schema(implementation = Metadata.class)
public interface MetadataOperator {

    @Schema(name = "name", description = "Metadata name", requiredMode = REQUIRED)
    @JsonProperty("name")
    String getName();

    @Schema(name = "generateName", description = "The name field will be generated automatically "
        + "according to the given generateName field")
    String getGenerateName();

    @Schema(name = "labels")
    @JsonProperty("labels")
    Map<String, String> getLabels();

    @Schema(name = "annotations")
    @JsonProperty("annotations")
    Map<String, String> getAnnotations();

    @Schema(name = "version", nullable = true)
    @JsonProperty("version")
    Long getVersion();

    @Schema(name = "creationTimestamp", nullable = true)
    @JsonProperty("creationTimestamp")
    Instant getCreationTimestamp();

    @Schema(name = "deletionTimestamp", nullable = true)
    @JsonProperty("deletionTimestamp")
    Instant getDeletionTimestamp();

    @Schema(name = "finalizers", nullable = true)
    Set<String> getFinalizers();

    void setName(String name);

    void setGenerateName(String generateName);

    void setLabels(Map<String, String> labels);

    void setAnnotations(Map<String, String> annotations);

    void setVersion(Long version);

    void setCreationTimestamp(Instant creationTimestamp);

    void setDeletionTimestamp(Instant deletionTimestamp);

    void setFinalizers(Set<String> finalizers);

    /**
     * Equals method for metadata.
     *
     * @param left metadata
     * @param right metadata
     * @return true if equals, false otherwise
     */
    static boolean equals(MetadataOperator left, MetadataOperator right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return Objects.equals(left.getName(), right.getName())
            && Objects.equals(left.getGenerateName(), right.getGenerateName())
            && Objects.equals(left.getLabels(), right.getLabels())
            && Objects.equals(left.getAnnotations(), right.getAnnotations())
            && Objects.equals(left.getCreationTimestamp(), right.getCreationTimestamp())
            && Objects.equals(left.getDeletionTimestamp(), right.getDeletionTimestamp())
            && Objects.equals(left.getVersion(), right.getVersion())
            && Objects.equals(left.getFinalizers(), right.getFinalizers());
    }

    /**
     * Hash code for metadata.
     *
     * @param metadata metadata
     * @return hash code
     */
    static int hashCode(MetadataOperator metadata) {
        return Objects.hash(
            metadata.getName(),
            metadata.getGenerateName(),
            metadata.getLabels(),
            metadata.getAnnotations(),
            metadata.getCreationTimestamp(),
            metadata.getDeletionTimestamp(),
            metadata.getVersion(),
            metadata.getFinalizers()
        );
    }
}
