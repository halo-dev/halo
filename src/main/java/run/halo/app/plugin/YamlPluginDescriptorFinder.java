package run.halo.app.plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.util.FileUtils;
import org.springframework.util.CollectionUtils;
import run.halo.app.core.extension.Plugin;

/**
 * Find a plugin descriptor for a plugin path.
 *
 * @author guqing
 * @see DefaultPluginDescriptor
 * @since 2.0.0
 */
@Slf4j
public class YamlPluginDescriptorFinder implements PluginDescriptorFinder {

    private final YamlPluginFinder yamlPluginFinder;

    public YamlPluginDescriptorFinder() {
        yamlPluginFinder = new YamlPluginFinder();
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath)
            && (Files.isDirectory(pluginPath)
            || FileUtils.isJarFile(pluginPath));
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        Plugin plugin = yamlPluginFinder.find(pluginPath);
        return convert(plugin);
    }

    private DefaultPluginDescriptor convert(Plugin plugin) {
        String pluginId = plugin.getMetadata().getName();
        Plugin.PluginSpec spec = plugin.getSpec();
        Plugin.PluginAuthor author = spec.getAuthor();
        String provider = (author == null ? StringUtils.EMPTY : author.getName());

        DefaultPluginDescriptor defaultPluginDescriptor =
            new DefaultPluginDescriptor(pluginId,
                spec.getDescription(),
                BasePlugin.class.getName(),
                spec.getVersion(),
                spec.getRequires(),
                provider,
                joinLicense(spec.getLicense()));
        // add dependencies
        spec.getPluginDependencies().forEach((pluginDepName, versionRequire) -> {
            PluginDependency dependency =
                new PluginDependency(String.format("%s@%s", pluginDepName, versionRequire));
            defaultPluginDescriptor.addDependency(dependency);
        });
        return defaultPluginDescriptor;
    }

    private String joinLicense(List<Plugin.License> licenses) {
        if (CollectionUtils.isEmpty(licenses)) {
            return StringUtils.EMPTY;
        }
        return licenses.stream()
            .map(Plugin.License::getName)
            .collect(Collectors.joining(","));
    }
}
