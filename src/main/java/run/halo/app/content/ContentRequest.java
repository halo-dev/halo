package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.Metadata;

/**
 * @author guqing
 * @since 2.0.0
 */
public record ContentRequest(@Schema(required = true) String raw,
                             @Schema(required = true) String content,
                             @Schema(required = true) String rawType) {

    public Snapshot toSnapshot() {
        Snapshot snapshot = new Snapshot();

        Metadata metadata = new Metadata();
        metadata.setName(UUID.randomUUID().toString());
        snapshot.setMetadata(metadata);

        Snapshot.SnapShotSpec snapShotSpec = new Snapshot.SnapShotSpec();
        snapShotSpec.setVersion(1);
        snapShotSpec.setRawType(rawType);
        snapShotSpec.setRawPatch(raw);
        snapShotSpec.setContentPatch(content);
        String displayVersion = Snapshot.displayVersionFrom(snapShotSpec.getVersion());
        snapShotSpec.setDisplayVersion(displayVersion);

        Snapshot.SubjectRef subjectRef = new Snapshot.SubjectRef();
        snapShotSpec.setSubjectRef(subjectRef);

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
