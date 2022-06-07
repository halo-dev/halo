package run.halo.app.plugin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;

/**
 * A custom resource for Plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Plugin extends AbstractExtension {

    @Schema(required = true)
    private PluginSpec spec;

    @Data
    public static class PluginSpec {

        private String displayName;

        private String version;

        private String author;

        private String logo;

        private Map<String, String> pluginDependencies = new HashMap<>(4);

        private String homepage;

        private String description;

        private String license;

        /**
         * SemVer format.
         */
        private String requires = "*";

        private String pluginClass = BasePlugin.class.getName();
    }
}
