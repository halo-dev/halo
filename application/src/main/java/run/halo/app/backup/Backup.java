package run.halo.app.backup;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "backup.halo.run", version = "v1alpha1", kind = "Backup",
    plural = "backups", singular = "backup")
public class Backup extends AbstractExtension {

    private Spec spec;

    private Status status;

    @Data
    public static class Spec {

    }

    @Data
    public static class Status {

        private Instant startTimestamp;

        private Instant completionTimestamp;

        @Schema(description = "bytes of archives")
        private Long size;

    }

}
