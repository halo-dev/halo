package run.halo.app.core.extension;

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

    @Schema(required = true)
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

        private String version;

        private String author;

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
    }
}
