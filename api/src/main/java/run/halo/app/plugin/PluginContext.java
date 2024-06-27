package run.halo.app.plugin;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pf4j.RuntimeMode;

/**
 * <p>This class will provide a context for the plugin, which will be used to store some
 * information about the plugin.</p>
 * <p>An instance of this class is provided to plugins in their constructor.</p>
 * <p>It's safe for plugins to keep a reference to the instance for later use.</p>
 * <p>This class facilitates communication with application and plugin manager.</p>
 * <p>Pf4j recommends that you use a custom PluginContext instead of PluginWrapper.</p>
 * <a href="https://github.com/pf4j/pf4j/blob/e4d7c7b9ea0c9a32179c3e33da1403228838944f/pf4j/src/main/java/org/pf4j/Plugin.java#L46">Use application custom PluginContext instead of PluginWrapper</a>
 *
 * @author guqing
 * @since 2.10.0
 */
@Getter
@Builder
@RequiredArgsConstructor
public class PluginContext {
    private final String name;

    private final String configMapName;

    private final String version;

    private final RuntimeMode runtimeMode;
}
