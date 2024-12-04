package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;

public interface PluginService {

    Mono<Void> installPresetPlugins();

    /**
     * Installs a plugin from a temporary Jar path.
     *
     * @param path is temporary jar path. Do not set the plugin home at here.
     * @return created plugin.
     */
    Mono<Plugin> install(Path path);

    Mono<Plugin> upgrade(String name, Path path);

    /**
     * <p>Reload a plugin by name.</p>
     * Note that this method will set <code>spec.enabled</code> to true it means that the plugin
     * will be started.
     *
     * @param name plugin name
     * @return an updated plugin reloaded from plugin path
     * @throws ServerWebInputException if plugin not found by the given name
     * @see Plugin.PluginSpec#setEnabled(Boolean)
     */
    Mono<Plugin> reload(String name);

    /**
     * Uglify js bundle from all enabled plugins to a single js bundle string.
     *
     * @return uglified js bundle
     */
    Flux<DataBuffer> uglifyJsBundle();

    /**
     * Uglify css bundle from all enabled plugins to a single css bundle string.
     *
     * @return uglified css bundle
     */
    Flux<DataBuffer> uglifyCssBundle();

    /**
     * <p>Generate js/css bundle version for cache control.</p>
     * This method will list all enabled plugins version and sign it to a string.
     *
     * @return signed js/css bundle version by all enabled plugins version.
     */
    Mono<String> generateBundleVersion();

    /**
     * Retrieves the JavaScript bundle for all enabled plugins.
     *
     * <p>This method combines the JavaScript bundles of all enabled plugins into a single bundle
     * and returns a representation of this bundle as a resource.
     * If the JavaScript bundle already exists and is up-to-date, the existing resource is
     * returned; otherwise, a new JavaScript bundle is generated.
     *
     * <p>Note: This method may perform IO operations and could potentially block, so it should be
     * used in a non-blocking environment.
     *
     * @param version The version of the CSS bundle to retrieve.
     * @return A {@code Mono<Resource>} object representing the JavaScript bundle. When this
     * {@code Mono} is subscribed to, it emits the JavaScript bundle resource if successful, or
     * an error signal if an error occurs.
     */
    Mono<Resource> getJsBundle(String version);

    /**
     * Retrieves the CSS bundle for all enabled plugins.
     *
     * <p>This method combines the CSS bundles of all enabled plugins into a single bundle and
     * returns a representation of this bundle as a resource.
     * If the CSS bundle already exists and is up-to-date, the existing resource is returned;
     * otherwise, a new CSS bundle is generated.
     *
     * <p>Note: This method may perform IO operations and could potentially block, so it should be
     * used in a non-blocking environment.
     *
     * @param version The version of the CSS bundle to retrieve.
     * @return A {@code Mono<Resource>} object representing the CSS bundle. When this {@code Mono
     * } is subscribed to, it emits the CSS bundle resource if successful, or an error signal if
     * an error occurs.
     */
    Mono<Resource> getCssBundle(String version);

    /**
     * Enables or disables a plugin by name.
     *
     * @param pluginName plugin name
     * @param requestToEnable request to enable or disable
     * @param wait wait for plugin to be enabled or disabled
     * @return updated plugin
     */
    Mono<Plugin> changeState(String pluginName, boolean requestToEnable, boolean wait);

    /**
     * Gets required dependencies of the given plugin.
     *
     * @param plugin the plugin to get dependencies from
     * {@link Plugin.PluginSpec#getPluginDependencies()}
     * @param predicate the predicate to filter by {@link PluginWrapper},such as enabled or disabled
     * @return plugin names of required dependencies
     */
    List<String> getRequiredDependencies(Plugin plugin,
        Predicate<PluginWrapper> predicate);
}
