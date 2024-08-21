package run.halo.app.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.file.Path;
import java.time.Instant;
import lombok.Data;

/**
 * Backup file.
 *
 * @author johnniang
 */
@Data
public class BackupFile {

    @JsonIgnore
    private Path path;

    /**
     * Filename of backup file.
     */
    private String filename;

    /**
     * Size of backup file.
     */
    private long size;

    /**
     * Last modified time of backup file.
     */
    private Instant lastModifiedTime;

}
