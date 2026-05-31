package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.pf4j.PluginState;
import org.springframework.util.Assert;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.infra.ConditionList;

/**
 * Plugin extension that describes an installed plugin and its runtime lifecycle state.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = "plugin.halo.run", version = "v1alpha1", kind = "Plugin", plural = "plugins", singular = "plugin")
@EqualsAndHashCode(callSuper = true)
public class Plugin extends AbstractExtension {

    public static final String SYSTEM_RESERVED_LABEL_KEY = "plugin.halo.run/system-reserved";

    public static final String BUILT_IN_KEEPER_FINALIZER = "plugin.halo.run/built-in-keeper";

    /** Desired plugin metadata, dependencies, configuration references, and enabled state. */
    @Schema(requiredMode = REQUIRED)
    private PluginSpec spec;

    /** Observed plugin runtime state reported by the plugin manager. */
    private @Nullable PluginStatus status;

    /**
     * Gets plugin status.
     *
     * @return empty object if status is null.
     */
    @JsonIgnore
    public PluginStatus statusNonNull() {
        if (this.status == null) {
            this.status = new PluginStatus();
        }
        return status;
    }

    /** Desired plugin metadata and configuration references. */
    @Data
    public static class PluginSpec {

        /** Display name shown for the plugin. */
        private String displayName;

        /**
         * plugin version.
         *
         * @see <a href="semver.org">semantic version</a>
         */
        @Schema(
                requiredMode = REQUIRED,
                pattern = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-("
                        + "(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\."
                        + "(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\"
                        + ".[0-9a-zA-Z-]+)*))?$")
        private String version;

        /** Plugin author metadata. */
        private PluginAuthor author;

        /** Logo URL or attachment URI for the plugin. */
        private String logo;

        /** Required plugin dependencies keyed by plugin name with version constraints as values. */
        private Map<String, String> pluginDependencies = new HashMap<>(4);

        /** Plugin homepage URL. */
        private String homepage;

        /** Source repository URL. */
        private String repo;

        /** Issue tracker URL. */
        private String issues;

        /** Human-readable plugin description. */
        private String description;

        /** Licenses declared by the plugin. */
        private List<License> license;

        /** SemVer format. */
        private String requires = "*";

        /** Whether the plugin should be enabled. */
        private Boolean enabled = false;

        /** Setting metadata.name used to render the plugin configuration form. */
        private String settingName;

        /** ConfigMap metadata.name storing plugin configuration values. */
        private String configMapName;
    }

    /**
     * In the future, we may consider using {@link run.halo.app.infra.model.License} instead of it. But now, replace it
     * will lead to incompatibility with downstream.
     */
    @Data
    public static class License {
        /** License name or identifier. */
        private String name;

        /** URL to the license text. */
        private String url;
    }

    /** Observed plugin lifecycle and runtime asset state. */
    @Data
    public static class PluginStatus {

        /** Current plugin lifecycle phase. */
        private Phase phase;

        /** Reconciliation conditions for the plugin. */
        private ConditionList conditions;

        /** Last time the plugin started successfully. */
        private Instant lastStartTime;

        /** Last PF4J probe state observed for the plugin. */
        private PluginState lastProbeState;

        /** JavaScript bundle entry path served for the plugin UI. */
        private String entry;

        /** Stylesheet bundle path served for the plugin UI. */
        private String stylesheet;

        /** Resolved logo URL or attachment URI for the plugin. */
        private String logo;

        /** URI location where the plugin artifact was loaded from. */
        private URI loadLocation;

        public static ConditionList nullSafeConditions(PluginStatus status) {
            Assert.notNull(status, "The status must not be null.");
            if (status.getConditions() == null) {
                status.setConditions(new ConditionList());
            }
            return status.getConditions();
        }
    }

    public enum Phase {
        PENDING,
        STARTING,
        CREATED,
        DISABLING,
        DISABLED,
        RESOLVED,
        STARTED,
        STOPPED,
        FAILED,
        UNKNOWN,
        ;
    }

    /** Plugin author metadata. */
    @Data
    @ToString
    public static class PluginAuthor {

        /** Author display name. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        /** Author website URL. */
        private String website;
    }
}
