package run.halo.app.infra.properties;

import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author guqing
 * @since 2022-04-12
 */
@Data
@ConfigurationProperties(prefix = "halo")
public class HaloProperties {

    private Set<String> initializeExtensionLocations;
}
