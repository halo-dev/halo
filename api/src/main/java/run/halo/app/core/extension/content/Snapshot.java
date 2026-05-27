package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

/**
 * Snapshot extension that stores a version of post or single page content.
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(
        group = Constant.GROUP,
        version = Constant.VERSION,
        kind = Snapshot.KIND,
        plural = "snapshots",
        singular = "snapshot")
@EqualsAndHashCode(callSuper = true)
public class Snapshot extends AbstractExtension {
    public static final String KIND = "Snapshot";

    /** Annotation key that marks a snapshot as a base snapshot storing full raw and rendered content. */
    public static final String KEEP_RAW_ANNO = "content.halo.run/keep-raw";

    /** Annotation key used to store the patched rendered content when a snapshot is restored for reading. */
    public static final String PATCHED_CONTENT_ANNO = "content.halo.run/patched-content";

    /** Annotation key used to store the patched raw content when a snapshot is restored for reading. */
    public static final String PATCHED_RAW_ANNO = "content.halo.run/patched-raw";

    /** Desired state of the snapshot, including the subject, content patches, and authorship information. */
    @Schema(requiredMode = REQUIRED)
    private SnapShotSpec spec;

    /** Content snapshot payload and metadata. */
    @Data
    public static class SnapShotSpec {

        /** Reference to the content extension that owns this snapshot, such as a Post or SinglePage. */
        @Schema(requiredMode = REQUIRED)
        private Ref subjectRef;

        /** Source format of the raw content, such as markdown, html, json, asciidoc, or latex. */
        @Schema(requiredMode = REQUIRED, minLength = 1, maxLength = 50)
        private String rawType;

        /** Full raw source content for a base snapshot, or a JSON-encoded line diff from the base raw content. */
        private String rawPatch;

        /** Full rendered content for a base snapshot, or a JSON-encoded line diff from the base rendered content. */
        private String contentPatch;

        /** Parent Snapshot metadata.name in the snapshot revision chain. */
        private String parentSnapshotName;

        /** Last time the snapshot content was modified. */
        private Instant lastModifyTime;

        /** User metadata.name of the snapshot owner. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String owner;

        /** User metadata.name values that contributed to this snapshot. */
        private Set<String> contributors;
    }

    public static void addContributor(Snapshot snapshot, String name) {
        Assert.notNull(name, "The username must not be null.");
        Set<String> contributors = snapshot.getSpec().getContributors();
        if (contributors == null) {
            contributors = new LinkedHashSet<>();
            snapshot.getSpec().setContributors(contributors);
        }
        contributors.add(name);
    }

    /**
     * Check if the given snapshot is a base snapshot.
     *
     * @param snapshot must not be null.
     * @return true if the given snapshot is a base snapshot; false otherwise.
     */
    public static boolean isBaseSnapshot(Snapshot snapshot) {
        var annotations = snapshot.getMetadata().getAnnotations();
        if (annotations == null) {
            return false;
        }
        return Boolean.parseBoolean(annotations.get(Snapshot.KEEP_RAW_ANNO));
    }

    public static String toSubjectRefKey(Ref subjectRef) {
        return subjectRef.getGroup() + "/" + subjectRef.getKind() + "/" + subjectRef.getName();
    }
}
