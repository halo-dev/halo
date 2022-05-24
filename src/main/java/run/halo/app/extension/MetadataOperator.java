package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

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

}
