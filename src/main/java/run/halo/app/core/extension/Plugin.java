package run.halo.app.core.extension;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.plugin.BasePlugin;

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

    @Data
    public static class PluginSpec {

        private String displayName;

        private String version;

        private String author;

        private String logo;

        private Map<String, String> pluginDependencies = new HashMap<>(4);

        private String homepage;

        private String description;

        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<License> license;

        /**
         * SemVer format.
         */
        private String requires = "*";

        private String pluginClass = BasePlugin.class.getName();

        private Boolean enabled = false;

        private List<String> extensionLocations;
    }

    @Getter
    @Setter
    public static class License {
        private String name;
        private String url;

        public License() {
        }

        public License(String name) {
            this.name = name;
            this.url = "";
        }
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
