package run.halo.app.migration;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/** Backup metadata and runtime state for an exported Halo data package. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "migration.halo.run", version = "v1alpha1", kind = "Backup", plural = "backups", singular = "backup")
public class Backup extends AbstractExtension {

    /** Desired backup options, such as the file format and expiration time. */
    private Spec spec = new Spec();

    /** Observed backup execution state and generated file information. */
    private Status status = new Status();

    /** Desired backup options. */
    @Data
    @Schema(name = "BackupSpec")
    public static class Spec {

        /** Backup file format. Currently, only zip is supported. */
        private String format;

        /** Time after which the generated backup should be considered expired. */
        private Instant expiresAt;
    }

    /** Observed backup execution state. */
    @Data
    @Schema(name = "BackupStatus")
    public static class Status {

        /** Current backup execution phase. */
        private Phase phase = Phase.PENDING;

        /** Time when backup execution started. */
        private Instant startTimestamp;

        /** Time when backup execution finished, regardless of success or failure. */
        private Instant completionTimestamp;

        /** Stable failure reason code for a failed backup. */
        private String failureReason;

        /** Human-readable failure message for a failed backup. */
        private String failureMessage;

        /** Size of the generated backup file in bytes. */
        private Long size;

        /** Name of the generated backup file. */
        private String filename;
    }

    public enum Phase {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED,
    }
}
