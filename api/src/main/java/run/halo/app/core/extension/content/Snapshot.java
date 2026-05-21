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
    @Schema(
            requiredMode = REQUIRED,
            description = "Desired state of the snapshot, including the subject, content patches, and authorship "
                    + "information.")
    private SnapShotSpec spec;

    @Data
    @Schema(description = "Content snapshot payload and metadata.")
    public static class SnapShotSpec {

        /** Reference to the content extension that owns this snapshot, such as a Post or SinglePage. */
        @Schema(
                requiredMode = REQUIRED,
                description = "Reference to the content extension that owns this snapshot, such as a Post or "
                        + "SinglePage.")
        private Ref subjectRef;

        /** such as: markdown | html | json | asciidoc | latex. */
        @Schema(
                requiredMode = REQUIRED,
                minLength = 1,
                maxLength = 50,
                description = "Type of the raw source content, such as markdown, html, json, asciidoc, or latex.")
        private String rawType;

        /** Raw source content for a base snapshot, or a JSON Patch from the base raw content for a derived snapshot. */
        @Schema(
                description = "Raw source content for a base snapshot, or a JSON Patch from the base raw content for "
                        + "a derived snapshot.")
        private String rawPatch;

        /**
         * Rendered content for a base snapshot, or a JSON Patch from the base rendered content for a derived snapshot.
         */
        @Schema(
                description = "Rendered content for a base snapshot, or a JSON Patch from the base rendered content "
                        + "for a derived snapshot.")
        private String contentPatch;

        /** Parent snapshot name in the snapshot revision chain. */
        @Schema(description = "Parent snapshot name in the snapshot revision chain.")
        private String parentSnapshotName;

        /** Last time the snapshot content was modified. */
        @Schema(description = "Last time the snapshot content was modified.")
        private Instant lastModifyTime;

        /** Username of the snapshot owner. */
        @Schema(requiredMode = REQUIRED, minLength = 1, description = "Username of the snapshot owner.")
        private String owner;

        /** Usernames that contributed to this snapshot. */
        @Schema(description = "Usernames that contributed to this snapshot.")
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
