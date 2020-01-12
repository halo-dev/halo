package run.halo.app.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static run.halo.app.model.support.HaloConst.*;
import static run.halo.app.utils.HaloUtils.ensureSuffix;


/**
 * Halo configuration properties.
 *
 * @author johnniang
 */
@Data
@ConfigurationProperties("halo")
public class HaloProperties {

    /**
     * Doc api disabled. (Default is true)
     */
    private boolean docDisabled = true;

    /**
     * Production env. (Default is true)
     */
    private boolean productionEnv = true;

    /**
     * Authentication enabled
     */
    private boolean authEnabled = true;

    /**
     * Admin path.
     */
    private String adminPath = "admin";

    /**
     * Work directory.
     */
    private String workDir = ensureSuffix(USER_HOME, FILE_SEPARATOR) + ".halo" + FILE_SEPARATOR;

    /**
     * Halo backup directory.(Not recommended to modify this config);
     */
    private String backupDir = ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-backup" + FILE_SEPARATOR;

    /**
     * Upload prefix.
     */
    private String uploadUrlPrefix = "upload";

    /**
     * Download Timeout.
     */
    private Duration downloadTimeout = Duration.ofSeconds(30);

    /**
     * cache store impl
     * memory
     * level
     */
    private String cache = "memory";


    public HaloProperties() throws IOException {
        // Create work directory if not exist
        Files.createDirectories(Paths.get(workDir));
        Files.createDirectories(Paths.get(backupDir));
    }
}
