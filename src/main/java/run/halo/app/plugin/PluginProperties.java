package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.pf4j.PluginLoader;
import org.pf4j.RuntimeMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for plugin.
 *
 * @author guqing
 * @see PluginAutoConfiguration
 */
@Data
@ConfigurationProperties(prefix = "halo.plugin")
public class PluginProperties {
    public static final String GRADLE_LIBS_DIR = "build/libs";

    /**
     * Auto start plugin when main app is ready.
     */
    private boolean autoStartPlugin = true;

    /**
     * The default plugin path is obtained through file scanning.
     * In the development mode, you can specify the plugin path as the project directory.
     */
    private List<Path> fixedPluginPath = new ArrayList<>();

    /**
     * Plugins disabled by default.
     */
    private String[] disabledPlugins = new String[0];

    /**
     * Plugins enabled by default, prior to `disabledPlugins`.
     */
    private String[] enabledPlugins = new String[0];

    /**
     * Set to true to allow requires expression to be exactly x.y.z. The default is false, meaning
     * that using an exact version x.y.z will implicitly mean the same as >=x.y.z.
     */
    private boolean exactVersionAllowed = false;

    /**
     * Extended Plugin Class Directory.
     */
    private List<String> classesDirectories = new ArrayList<>();

    /**
     * Extended Plugin Jar Directory.
     */
    private List<String> libDirectories = new ArrayList<>(List.of(GRADLE_LIBS_DIR));

    /**
     * Runtime Mode：development/deployment.
     */
    private RuntimeMode runtimeMode = RuntimeMode.DEPLOYMENT;

    /**
     * Plugin root directory: default “plugins”; when non-jar mode plugin, the value should be an
     * absolute directory address.
     */
    private String pluginsRoot;

    /**
     * Allows providing custom plugin loaders.
     */
    private Class<PluginLoader> customPluginLoader;
}
