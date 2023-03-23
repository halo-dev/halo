package run.halo.app.core.extension.content;

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

    @Schema(required = true)
    private SnapShotSpec spec;

    @Data
    public static class SnapShotSpec {

        @Schema(required = true)
        private Ref subjectRef;

        /**
         * such as: markdown | html | json | asciidoc | latex.
         */
        @Schema(required = true, minLength = 1, maxLength = 50)
        private String rawType;

        private String rawPatch;

        private String contentPatch;

        private String parentSnapshotName;

        private Instant lastModifyTime;

        @Schema(required = true, minLength = 1)
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

}
