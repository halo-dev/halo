package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Snapshot;

/**
 * Full content reconstructed from a content snapshot and its base snapshot.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Full content reconstructed from a content snapshot and its base snapshot.")
@Data
@Builder
public class ContentWrapper {
    @Schema(description = "Name of the snapshot used to build this content.")
    private String snapshotName;

    @Schema(description = "Source text stored by the editor.")
    private String raw;

    @Schema(description = "Rendered HTML or normalized content derived from raw.")
    private String content;

    @Schema(description = "Editor/source format of the raw content, for example HTML or Markdown.")
    private String rawType;

    public static ContentWrapper patchSnapshot(Snapshot patchSnapshot, Snapshot baseSnapshot) {
        Assert.notNull(baseSnapshot, "The baseSnapshot must not be null.");
        String baseSnapshotName = baseSnapshot.getMetadata().getName();
        if (StringUtils.equals(patchSnapshot.getMetadata().getName(), baseSnapshotName)) {
            return ContentWrapper.builder()
                    .snapshotName(patchSnapshot.getMetadata().getName())
                    .raw(patchSnapshot.getSpec().getRawPatch())
                    .content(patchSnapshot.getSpec().getContentPatch())
                    .rawType(patchSnapshot.getSpec().getRawType())
                    .build();
        }
        String patchedContent = PatchUtils.applyPatch(
                baseSnapshot.getSpec().getContentPatch(),
                patchSnapshot.getSpec().getContentPatch());
        String patchedRaw = PatchUtils.applyPatch(
                baseSnapshot.getSpec().getRawPatch(), patchSnapshot.getSpec().getRawPatch());
        return ContentWrapper.builder()
                .snapshotName(patchSnapshot.getMetadata().getName())
                .raw(patchedRaw)
                .content(patchedContent)
                .rawType(patchSnapshot.getSpec().getRawType())
                .build();
    }
}
