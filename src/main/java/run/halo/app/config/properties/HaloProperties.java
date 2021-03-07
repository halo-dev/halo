package run.halo.app.config.properties;

import static run.halo.app.model.support.HaloConst.FILE_SEPARATOR;
import static run.halo.app.model.support.HaloConst.TEMP_DIR;
import static run.halo.app.model.support.HaloConst.USER_HOME;
import static run.halo.app.utils.HaloUtils.ensureSuffix;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import run.halo.app.model.enums.Mode;


/**
 * Halo configuration properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-15
 */
@Data
@ConfigurationProperties("halo")
public class HaloProperties {

    /**
     * Doc api disabled. (Default is true)
     */
    @Deprecated
    private boolean docDisabled = true;

    /**
     * Production env. (Default is true)
     */
    @Deprecated
    private boolean productionEnv = true;

    /**
     * Authentication enabled.
     */
    private boolean authEnabled = true;

    /**
     * Halo startup mode.
     */
    private Mode mode = Mode.PRODUCTION;

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
    private String backupDir =
        ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-backup" + FILE_SEPARATOR;

    /**
     * Halo backup markdown directory.(Not recommended to modify this config);
     */
    private String backupMarkdownDir =
        ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-backup-markdown" + FILE_SEPARATOR;

    /**
     * Halo data export directory.
     */
    private String dataExportDir =
        ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-data-export" + FILE_SEPARATOR;

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
}
