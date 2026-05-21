package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.experimental.Accessors;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.MetadataOperator;

/**
 * Lightweight snapshot representation returned by content snapshot list endpoints.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Content snapshot list item with metadata and display-oriented snapshot fields.")
@Data
@Accessors(chain = true)
public class ListedSnapshotDto {
    @Schema(
            description = "Snapshot metadata, including name, labels, annotations, version, and timestamps.",
            requiredMode = REQUIRED)
    private MetadataOperator metadata;

    @Schema(description = "Snapshot fields shown in the console snapshot list.", requiredMode = REQUIRED)
    private Spec spec;

    @Data
    @Accessors(chain = true)
    @Schema(name = "ListedSnapshotSpec", description = "Display fields for a content snapshot list item.")
    public static class Spec {
        @Schema(description = "User name of the snapshot owner.", requiredMode = REQUIRED)
        private String owner;

        @Schema(description = "Last modification time of the snapshot content.")
        private Instant modifyTime;
    }

    /** Creates from snapshot. */
    public static ListedSnapshotDto from(Snapshot snapshot) {
        return new ListedSnapshotDto()
                .setMetadata(snapshot.getMetadata())
                .setSpec(new Spec()
                        .setOwner(snapshot.getSpec().getOwner())
                        .setModifyTime(snapshot.getSpec().getLastModifyTime()));
    }
}
