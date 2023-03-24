package run.halo.app.core.extension.service;

import java.nio.file.Path;
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

}
