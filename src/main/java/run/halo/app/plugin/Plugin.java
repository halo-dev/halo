package run.halo.app.plugin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.pf4j.PluginDependency;
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

        protected String displayName;

        protected String version;

        protected String author;

        protected String logo;

        protected List<PluginDependency> dependencies = new ArrayList<>(4);

        protected String homepage;

        protected String description;

        protected String license;

        /**
         * SemVer format.
         */
        protected String requires = "*";

        protected String pluginClass = BasePlugin.class.getName();
    }
}
