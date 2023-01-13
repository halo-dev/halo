package run.halo.app.infra;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Default implementation of system version supplier.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class DefaultSystemVersionSupplier implements SystemVersionSupplier {
    private static final String DEFAULT_VERSION = "0.0.0";

    @Nullable
    private BuildProperties buildProperties;

    @Autowired(required = false)
    public void setBuildProperties(@Nullable BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public Version get() {
        if (buildProperties == null) {
            return Version.valueOf(DEFAULT_VERSION);
        }
        String projectVersion =
            StringUtils.defaultString(buildProperties.getVersion(), DEFAULT_VERSION);
        return Version.valueOf(projectVersion);
    }
}
