package run.halo.app.core.extension.service;

import java.nio.file.Path;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;

public interface PluginService {

    Flux<Plugin> getPresets();

    /**
     * Gets a plugin information by preset name from plugin presets.
     *
     * @param presetName is preset name of plugin.
     * @return plugin preset information.
     */
    Mono<Plugin> getPreset(String presetName);

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
     * @see run.halo.app.plugin.HaloPluginManager#reloadPlugin(String)
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
     * <p>Generate js bundle version for cache control.</p>
     * This method will list all enabled plugins version and sign it to a string.
     *
     * @return signed js bundle version by all enabled plugins version.
     */
    Mono<String> generateJsBundleVersion();
}
