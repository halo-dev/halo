package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;

/**
 * @author guqing
 * @since 2.0.0
 */
public record ContentRequest(@Schema(required = true) Ref subjectRef,
                             String headSnapshotName,
                             @Schema(required = true) String raw,
                             @Schema(required = true) String content,
                             @Schema(required = true) String rawType) {

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
