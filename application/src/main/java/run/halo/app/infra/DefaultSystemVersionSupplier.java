package run.halo.app.infra;

import com.github.zafarkhaja.semver.Version;
import java.util.Objects;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
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

    private final ObjectProvider<BuildProperties> buildProperties;

    public DefaultSystemVersionSupplier(ObjectProvider<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public Version get() {
        var properties = buildProperties.getIfUnique();
        if (properties == null) {
            return Version.parse(DEFAULT_VERSION);
        }
        var projectVersion = Objects.toString(properties.getVersion(), DEFAULT_VERSION);
        return Version.parse(projectVersion);
    }
}
