package run.halo.app.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PatchUtils;
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
@GVK(group = "content.halo.run", version = "v1alpha1",
    kind = Snapshot.KIND, plural = "snapshots", singular = "snapshot")
@EqualsAndHashCode(callSuper = true)
public class Snapshot extends AbstractExtension {
    public static final String KIND = "Snapshot";
    public static final String PUBLISHED_LABEL = "content.halo.run/published";

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

    public static String displayVersionFrom(Integer version) {
        Assert.notNull(version, "The version must not be null");
        return "v" + version;
    }

    @JsonIgnore
    public boolean isPublished() {
        Map<String, String> labels = getMetadata().getLabels();
        return labels != null && labels.getOrDefault(PUBLISHED_LABEL, "false").equals("true");
    }

    @JsonIgnore
    public void addContributor(String name) {
        Assert.notNull(name, "The username must not be null.");
        Set<String> contributors = spec.getContributorsOrDefault();
        contributors.add(name);
    }

    @JsonIgnore
    public ContentWrapper applyPatch(Snapshot baseSnapshot) {
        Assert.notNull(baseSnapshot, "The baseSnapshot must not be null.");
        if (this.spec.version == 1) {
            return ContentWrapper.builder()
                .snapshotName(this.getMetadata().getName())
                .version(this.spec.version)
                .raw(this.spec.rawPatch)
                .content(this.spec.contentPatch)
                .rawType(this.spec.rawType)
                .build();
        }
        String patchedContent =
            PatchUtils.applyPatch(baseSnapshot.getSpec().getContentPatch(), this.spec.contentPatch);
        String patchedRaw =
            PatchUtils.applyPatch(baseSnapshot.getSpec().getRawPatch(), this.spec.rawPatch);
        return ContentWrapper.builder()
            .snapshotName(this.getMetadata().getName())
            .version(this.spec.version)
            .raw(patchedRaw)
            .content(patchedContent)
            .rawType(this.spec.rawType)
            .build();
    }

    public static void putPublishedLabel(Map<String, String> labels) {
        Assert.notNull(labels, "The labels must not be null.");
        labels.put(PUBLISHED_LABEL, "true");
    }
}
