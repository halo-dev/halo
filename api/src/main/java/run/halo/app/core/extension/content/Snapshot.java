package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

/**
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Snapshot.KIND,
    plural = "snapshots", singular = "snapshot")
@EqualsAndHashCode(callSuper = true)
public class Snapshot extends AbstractExtension {
    public static final String KIND = "Snapshot";
    public static final String KEEP_RAW_ANNO = "content.halo.run/keep-raw";
    public static final String PATCHED_CONTENT_ANNO = "content.halo.run/patched-content";
    public static final String PATCHED_RAW_ANNO = "content.halo.run/patched-raw";

    @Schema(requiredMode = REQUIRED)
    private SnapShotSpec spec;

    @Data
    public static class SnapShotSpec {

        @Schema(requiredMode = REQUIRED)
        private Ref subjectRef;

        /**
         * such as: markdown | html | json | asciidoc | latex.
         */
        @Schema(requiredMode = REQUIRED, minLength = 1, maxLength = 50)
        private String rawType;

        private String rawPatch;

        private String contentPatch;

        private String parentSnapshotName;

        private Instant lastModifyTime;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String owner;

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
    public static boolean isBaseSnapshot(@NonNull Snapshot snapshot) {
        var annotations = snapshot.getMetadata().getAnnotations();
        if (annotations == null) {
            return false;
        }
        return Boolean.parseBoolean(annotations.get(Snapshot.KEEP_RAW_ANNO));
    }

}
