package run.halo.app.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

/**
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = "content.halo.run", version = "v1alpha1",
    kind = Snapshot.KIND, plural = "snapshots", singular = "snapshot")
@EqualsAndHashCode(callSuper = true)
public class Snapshot extends AbstractExtension {
    public static final String KIND = "Snapshot";

    @Schema(required = true)
    private SnapShotSpec spec;

    @Data
    public static class SnapShotSpec {

        @Schema(required = true)
        private SubjectRef subjectRef;

        /**
         * such as: markdown | html | json | asciidoc | latex
         */
        @Schema(required = true, minLength = 1, maxLength = 50)
        private String rawType;

        private String rawPatch;

        private String contentPatch;

        private String parentSnapshotName;

        @Schema(required = true)
        private String displayVersion;

        @Schema(required = true, defaultValue = "1")
        private Integer version;

        private Instant publishTime;

        private Set<String> contributors;

        @JsonIgnore
        public Set<String> getContributorsOrDefault() {
            if (this.contributors == null) {
                this.contributors = new LinkedHashSet<>();
            }
            return this.contributors;
        }
    }

    @Data
    public static class SubjectRef {
        @Schema(required = true)
        private String kind;

        @Schema(required = true)
        private String name;
    }

    public static String displayVersionFrom(Integer version) {
        Assert.notNull(version, "The version must not be null");
        return "v" + version;
    }

    @JsonIgnore
    public boolean isPublished() {
        return this.spec.getPublishTime() != null;
    }

    @JsonIgnore
    public void addContributor(String name) {
        Assert.notNull(name, "The username must not be null.");
        Set<String> contributors = spec.getContributorsOrDefault();
        contributors.add(name);
    }

    @JsonIgnore
    public void setSubjectRef(String kind, String name) {
        Assert.notNull(kind, "The subject kind must not be null.");
        Assert.notNull(name, "The subject name must not be null.");
        if (spec.subjectRef == null) {
            spec.subjectRef = new SubjectRef();
        }
        spec.subjectRef.setKind(kind);
        spec.subjectRef.setName(name);
    }
}
