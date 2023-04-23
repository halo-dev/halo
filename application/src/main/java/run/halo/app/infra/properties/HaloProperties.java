package run.halo.app.infra.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

/**
 * @author guqing
 * @since 2022-04-12
 */
@Data
@ConfigurationProperties(prefix = "halo")
@Validated
public class HaloProperties implements Validator {

    @NotNull
    private Path workDir;

    /**
     * External URL must be a URL and it can be null.
     */
    private URL externalUrl;

    /**
     * Indicates if we use absolute permalink to post, page, category, tag and so on.
     */
    private boolean useAbsolutePermalink;

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

    @Override
    public boolean supports(Class<?> clazz) {
        return HaloProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var props = (HaloProperties) target;
        if (props.isUseAbsolutePermalink() && props.getExternalUrl() == null) {
            errors.rejectValue("externalUrl", "external-url.required.when-using-absolute-permalink",
                "External URL is required when property `use-absolute-permalink` is set to true.");
        }
        SecurityProperties.Initializer.validateUsername(props.getSecurity().getInitializer(),
            errors);
    }
}
