package run.halo.app.migration;

import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "migration.halo.run", version = "v1alpha1", kind = "Backup",
    plural = "backups", singular = "backup")
public class Backup extends AbstractExtension {

    private Spec spec;

    private Status status;

    @Data
    public static class Spec {

        private String format;

        private Instant autoDeleteWhen;

    }

    @Data
    public static class Status {

        private Phase phase = Phase.PENDING;

        private Instant startTimestamp;

        private Instant completionTimestamp;

        private String failureReason;

        private String failureMessage;

        /**
         * Size of backup file. Data unit: byte
         */
        private Long size;

        /**
         * Name of backup file.
         */
        private String filename;
    }

    public enum Phase {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED,
    }

}
