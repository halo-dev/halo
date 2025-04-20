package run.halo.app.infra.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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
    @NestedConfigurationProperty
    private final ExtensionProperties extension = new ExtensionProperties();

    @Valid
    @NestedConfigurationProperty
    private final SecurityProperties security = new SecurityProperties();

    @Valid
    @NestedConfigurationProperty
    private final ConsoleProperties console = new ConsoleProperties();

    @Valid
    @NestedConfigurationProperty
    private final UcProperties uc = new UcProperties();

    @Valid
    @NestedConfigurationProperty
    private final ThemeProperties theme = new ThemeProperties();

    @Valid
    @NestedConfigurationProperty
    private final AttachmentProperties attachment = new AttachmentProperties();
    
    /**
     * Distributed deployment properties.
     */
    @Valid
    @NestedConfigurationProperty
    private final DistributedProperties distributed = new DistributedProperties();
    
    /**
     * Properties for distributed deployment: messaging and scheduling.
     */
    public static class DistributedProperties {
        /** Enable distributed features (messaging, cache sync, scheduling). */
        private boolean enabled = false;
        /** Redis Stream key for change events. */
        private String streamKey = "halo:distributed:stream";
        /** Consumer group name for Redis Stream. */
        private String consumerGroup = "halo-consumer-group";
        /** Prefix for ShedLock distributed scheduler locks. */
        private String schedulerLockPrefix = "halo:scheduler-lock:";

        public boolean isEnabled() {
            return enabled;
        }
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        public String getStreamKey() {
            return streamKey;
        }
        public void setStreamKey(String streamKey) {
            this.streamKey = streamKey;
        }
        public String getConsumerGroup() {
            return consumerGroup;
        }
        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }
        public String getSchedulerLockPrefix() {
            return schedulerLockPrefix;
        }
        public void setSchedulerLockPrefix(String schedulerLockPrefix) {
            this.schedulerLockPrefix = schedulerLockPrefix;
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return HaloProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var props = (HaloProperties) target;
        var externalUrl = props.getExternalUrl();
        if (props.isUseAbsolutePermalink() && externalUrl == null) {
            errors.rejectValue("externalUrl", "external-url.required.when-using-absolute-permalink",
                "External URL is required when property `use-absolute-permalink` is set to true.");
        }
        // check if the external URL is a http or https URL and is not an opaque URL.
        if (externalUrl != null && !isValidExternalUrl(externalUrl)) {
            errors.rejectValue("externalUrl", "external-url.invalid-format",
                "External URL must be a http or https URL.");
        }
    }

    private boolean isValidExternalUrl(URL externalUrl) {
        try {
            var uri = externalUrl.toURI();
            return !uri.isOpaque()
                && uri.getAuthority() != null
                && Set.of("http", "https").contains(uri.getScheme());
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
