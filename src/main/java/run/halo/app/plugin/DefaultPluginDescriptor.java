package run.halo.app.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.pf4j.Plugin;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import run.halo.app.extension.AbstractExtension;

/**
 * A default implementation for {@link PluginDescriptor}, it contains information about a plugin.
 *
 * @author guqing
 * @see YamlPluginDescriptorFinder
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DefaultPluginDescriptor extends AbstractExtension implements PluginDescriptor {

    @Schema(required = true)
    private PluginSpec spec;

    @Override
    @JsonIgnore
    public String getPluginId() {
        return getMetadata().getName();
    }

    @Override
    @JsonIgnore
    public String getPluginDescription() {
        return spec.description;
    }

    @Override
    @JsonIgnore
    public String getPluginClass() {
        return spec.pluginClass;
    }

    @Override
    @JsonIgnore
    public String getVersion() {
        return spec.version;
    }

    @Override
    @JsonIgnore
    public String getRequires() {
        return spec.requires;
    }

    @Override
    @JsonIgnore
    public String getProvider() {
        return spec.author;
    }

    @Override
    @JsonIgnore
    public String getLicense() {
        return spec.license;
    }

    @Override
    public List<PluginDependency> getDependencies() {
        return spec.dependencies;
    }

    protected PluginDescriptor setDependencies(String dependencies) {
        spec.dependencies = new ArrayList<>();

        if (dependencies != null) {
            dependencies = dependencies.trim();
            if (!dependencies.isEmpty()) {
                String[] tokens = dependencies.split(",");
                for (String dependency : tokens) {
                    dependency = dependency.trim();
                    if (!dependency.isEmpty()) {
                        spec.dependencies.add(new PluginDependency(dependency));
                    }
                }
            }
        }

        return this;
    }


    public void addDependency(PluginDependency dependency) {
        spec.dependencies.add(dependency);
    }

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

        protected String pluginClass = Plugin.class.getName();
    }
}
