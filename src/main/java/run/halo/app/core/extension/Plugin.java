package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.pf4j.PluginState;
import org.springframework.lang.NonNull;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * A custom resource for Plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = "plugin.halo.run", version = "v1alpha1", kind = "Plugin", plural = "plugins",
    singular = "plugin")
@EqualsAndHashCode(callSuper = true)
public class Plugin extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private PluginSpec spec;

    private PluginStatus status;

    /**
     * Gets plugin status.
     *
     * @return empty object if status is null.
     */
    @NonNull
    @JsonIgnore
    public PluginStatus statusNonNull() {
        if (this.status == null) {
            this.status = new PluginStatus();
        }
        return status;
    }

    @Data
    public static class PluginSpec {

        private String displayName;

        /**
         * plugin version.
         *
         * @see <a href="semver.org">semantic version</a>
         */
        @Schema(required = true, pattern = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-("
            + "(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\."
            + "(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\"
            + ".[0-9a-zA-Z-]+)*))?$")
        private String version;

        private PluginAuthor author;

        private String logo;

        private Map<String, String> pluginDependencies = new HashMap<>(4);

        private String homepage;

        private String description;

        private List<License> license;

        /**
         * SemVer format.
         */
        private String requires = "*";

        @Deprecated
        private String pluginClass;

        private Boolean enabled = false;

        private String settingName;

        private String configMapName;
    }

    @Getter
    @Setter
    public static class License {
        private String name;
        private String url;
    }

    @Data
    public static class PluginStatus {

        private PluginState phase;

        private String reason;

        private String message;

        private Instant lastStartTime;

        private Instant lastTransitionTime;

        private String entry;

        private String stylesheet;

        private String logo;
    }

    @Data
    @ToString
    public static class PluginAuthor {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        private String website;
    }

    @JsonIgnore
    public String generateFileName() {
        return String.format("%s-%s.jar", getMetadata().getName(), spec.getVersion());
    }
}
