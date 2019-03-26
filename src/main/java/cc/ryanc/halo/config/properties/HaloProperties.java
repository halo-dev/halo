package cc.ryanc.halo.config.properties;

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

    private final static String USER_HOME = System.getProperty("user.home");

    /**
     * Doc api disabled. (Default is true)
     */
    private Boolean docDisabled = true;

    /**
     * Production env. (Default is true)
     */
    private Boolean productionEnv = true;

    /**
     * Work directory.
     */
    private String workDir = USER_HOME + "/halo/";
}
