package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;

/**
 * Internal content request used by content services to create or update snapshots.
 *
 * @author guqing
 * @since 2.0.0
 */
@Builder
@Schema(description = "Snapshot content request used when Halo creates or updates content snapshots.")
public record ContentRequest(
        @Schema(description = "Reference to the post or single page that owns the content.", requiredMode = REQUIRED)
        Ref subjectRef,

        /** Current head snapshot name used as the update target. */
        String headSnapshotName,

        @Schema(description = "Expected metadata version of the current head snapshot.", requiredMode = NOT_REQUIRED)
        Long version,

        @Schema(description = "Source text stored by the editor.", requiredMode = REQUIRED)
        String raw,

        @Schema(description = "Rendered HTML or normalized content derived from raw.", requiredMode = REQUIRED)
        String content,

        @Schema(
                description = "Editor/source format of the raw content, for example HTML or Markdown.",
                requiredMode = REQUIRED)
        String rawType) {

    public Snapshot toSnapshot() {
        final Snapshot snapshot = new Snapshot();

        Metadata metadata = new Metadata();
        metadata.setAnnotations(new HashMap<>());
        snapshot.setMetadata(metadata);

        Snapshot.SnapShotSpec snapShotSpec = new Snapshot.SnapShotSpec();
        snapShotSpec.setSubjectRef(subjectRef);

        snapShotSpec.setRawType(rawType);
        snapShotSpec.setRawPatch(StringUtils.defaultString(raw()));
        snapShotSpec.setContentPatch(StringUtils.defaultString(content()));

        snapshot.setSpec(snapShotSpec);
        return snapshot;
    }

    public String rawPatchFrom(String originalRaw) {
        // originalRaw content from v1
        return PatchUtils.diffToJsonPatch(originalRaw, this.raw);
    }

    public String contentPatchFrom(String originalContent) {
        // originalContent from v1
        return PatchUtils.diffToJsonPatch(originalContent, this.content);
    }
}
