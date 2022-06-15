package run.halo.app.extension;

import java.time.Instant;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Metadata of Extension.
 *
 * @author johnniang
 */
@Data
@EqualsAndHashCode
public class Metadata implements MetadataOperator {

    /**
     * Metadata name. The name is unique globally.
     */
    private String name;

    /**
     * Labels are like key-value format.
     */
    private Map<String, String> labels;

    /**
     * Annotations are like key-value format.
     */
    private Map<String, String> annotations;

    /**
     * Current version of the Extension. It will be bumped up every update.
     */
    private Long version;

    /**
     * Creation timestamp of the Extension.
     */
    private Instant creationTimestamp;

    /**
     * Deletion timestamp of the Extension.
     */
    private Instant deletionTimestamp;

}
