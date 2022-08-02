package run.halo.app.infra.properties;

import java.nio.file.Path;
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

    private Path workDir;

    private Set<String> initialExtensionLocations = new HashSet<>();

    /**
     * This property could stop initializing required Extensions defined in classpath.
     * See {@link run.halo.app.infra.ExtensionResourceInitializer#REQUIRED_EXTENSION_LOCATIONS}
     * for more.
     */
    private boolean requiredExtensionDisabled;

    private final ExtensionProperties extension = new ExtensionProperties();

    private final SecurityProperties security = new SecurityProperties();
}
