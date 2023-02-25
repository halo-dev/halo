package run.halo.app.core.extension.service;

import reactor.core.publisher.Flux;
import run.halo.app.core.extension.Plugin;

public interface PluginService {

    Flux<Plugin> getPresets();

}
