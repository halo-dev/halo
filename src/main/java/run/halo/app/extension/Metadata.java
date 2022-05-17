package run.halo.app.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.Data;

/**
 * Metadata of Extension.
 *
 * @author johnniang
 */
@Data
public class Metadata {

    /**
     * Metadata name. The name is unique globally.
     */
    @Schema(required = true)
    private String name;

    /**
     * Labels are like key-value format.
     */
    @Schema(nullable = true)
    private Map<String, String> labels;

    /**
     * Annotations are like key-value format.
     */
    @Schema(nullable = true)
    private Map<String, String> annotations;

    /**
     * Current version of the Extension. It will be bumped up every update.
     */
    @Schema(nullable = true)
    private Long version;

    /**
     * Creation timestamp of the Extension.
     */
    @Schema(nullable = true)
    private Instant creationTimestamp;

    /**
     * Deletion timestamp of the Extension.
     */
    @Schema(nullable = true)
    private Instant deletionTimestamp;

}
