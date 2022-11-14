package run.halo.app.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PatchUtils;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ExtensionUtil;
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
    public static final String LAST_MODIFY_TIME_ANNO = "content.halo.run/last-modify-time";
    public static final String VERSION_ANNO = "content.halo.run/version";
    public static final String VERSION_LABEL = "content.halo.run/version";

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

        private Set<String> contributors;

        @JsonIgnore
        public Set<String> getContributorsOrDefault() {
            if (this.contributors == null) {
                this.contributors = new LinkedHashSet<>();
            }
            return this.contributors;
        }
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
        Integer version = getVersionAnno(this);
        if (version == 1) {
            return ContentWrapper.builder()
                .snapshotName(this.getMetadata().getName())
                .version(version)
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
            .version(version)
            .raw(patchedRaw)
            .content(patchedContent)
            .rawType(this.spec.rawType)
            .build();
    }

    /**
     * Get snapshot version.
     */
    public static Integer getVersionAnno(Snapshot snapshot) {
        String version = ExtensionUtil.nullSafeLabels(snapshot)
            .getOrDefault(VERSION_ANNO, "1");
        return Integer.parseInt(version);
    }
}
