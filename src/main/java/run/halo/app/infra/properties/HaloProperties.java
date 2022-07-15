package run.halo.app.infra.properties;

import java.util.HashSet;
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

    private Set<String> initialExtensionLocations = new HashSet<>();

    private final ExtensionProperties extension = new ExtensionProperties();

    private final SecurityProperties security = new SecurityProperties();

}
