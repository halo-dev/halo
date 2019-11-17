package run.halo.app.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import run.halo.app.model.support.HaloConst;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


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
    private String adminPath = "/admin";

    /**
     * Halo backup directory.(Not recommended to modify this config);
     */
    private String backupDir = HaloConst.TEMP_DIR + "/halo-backup/";

    /**
     * Work directory.
     */
    private String workDir = HaloConst.USER_HOME + "/.halo/";

    /**
     * Upload prefix.
     */
    private String uploadUrlPrefix = "/upload";

    /**
     * backup prefix.
     */
    private String backupUrlPrefix = "/backup";

    public HaloProperties() throws IOException {
        // Create work directory if not exist
        Files.createDirectories(Paths.get(workDir));
        Files.createDirectories(Paths.get(backupDir));
    }
}
