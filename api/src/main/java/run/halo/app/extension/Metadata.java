package run.halo.app.extension;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Metadata of Extension.
 *
 * @author johnniang
 */
@Data
@EqualsAndHashCode(exclude = "version")
public class Metadata implements MetadataOperator {

    /**
     * Metadata name. The name is unique globally.
     */
    private String name;

    /**
     * Generate name is for generating metadata name automatically.
     */
    private String generateName;

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

    private Set<String> finalizers;

}
