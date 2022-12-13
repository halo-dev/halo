package run.halo.app.infra.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author guqing
 * @since 2022-04-12
 */
@Data
@ConfigurationProperties(prefix = "halo")
@Validated
public class HaloProperties {

    @NotNull
    private Path workDir;

    @NotNull
    private URI externalUrl;

    private Set<String> initialExtensionLocations = new HashSet<>();

    /**
     * This property could stop initializing required Extensions defined in classpath.
     * See {@link run.halo.app.infra.ExtensionResourceInitializer#REQUIRED_EXTENSION_LOCATIONS}
     * for more.
     */
    private boolean requiredExtensionDisabled;

    @Valid
    private final ExtensionProperties extension = new ExtensionProperties();

    @Valid
    private final SecurityProperties security = new SecurityProperties();

    @Valid
    private final ConsoleProperties console = new ConsoleProperties();

    @Valid
    private final ThemeProperties theme = new ThemeProperties();

    @Valid
    private final AttachmentProperties attachment = new AttachmentProperties();
}
