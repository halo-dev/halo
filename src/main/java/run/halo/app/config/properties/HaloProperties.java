package run.halo.app.config.properties;

import run.halo.app.model.support.HaloConst;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
     * Work directory.
     */
    private String workDir = HaloConst.USER_HOME + "/halo/";
}
