package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.experimental.Accessors;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.MetadataOperator;

@Data
@Accessors(chain = true)
public class ListedSnapshotDto {
    @Schema(requiredMode = REQUIRED)
    private MetadataOperator metadata;

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Data
    @Accessors(chain = true)
    @Schema(name = "ListedSnapshotSpec")
    public static class Spec {
        @Schema(requiredMode = REQUIRED)
        private String owner;

        private Instant modifyTime;
    }

    /**
     * Creates from snapshot.
     */
    public static ListedSnapshotDto from(Snapshot snapshot) {
        return new ListedSnapshotDto()
            .setMetadata(snapshot.getMetadata())
            .setSpec(new Spec()
                .setOwner(snapshot.getSpec().getOwner())
                .setModifyTime(snapshot.getSpec().getLastModifyTime())
            );
    }
}
