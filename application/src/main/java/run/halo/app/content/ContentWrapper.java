package run.halo.app.content;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Snapshot;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
public class ContentWrapper {
    private String snapshotName;
    private String raw;
    private String content;
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
        String patchedContent = PatchUtils.applyPatch(baseSnapshot.getSpec().getContentPatch(),
            patchSnapshot.getSpec().getContentPatch());
        String patchedRaw = PatchUtils.applyPatch(baseSnapshot.getSpec().getRawPatch(),
            patchSnapshot.getSpec().getRawPatch());
        return ContentWrapper.builder()
            .snapshotName(patchSnapshot.getMetadata().getName())
            .raw(patchedRaw)
            .content(patchedContent)
            .rawType(patchSnapshot.getSpec().getRawType())
            .build();
    }
}
