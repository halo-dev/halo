package run.halo.app.core.extension.service.impl;

import java.io.IOException;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.service.PluginService;
import run.halo.app.plugin.YamlPluginFinder;

@Component
public class PluginServiceImpl implements PluginService {

    private static final String PRESETS_LOCATION_PATTERN = "classpath:/presets/plugins/*.jar";

    @Override
    public Flux<Plugin> getPresets() {
        // list presets from classpath
        return Flux.defer(() -> getPresetJars()
            .map(this::toPath)
            .map(path -> new YamlPluginFinder().find(path)));
    }

    private Flux<Resource> getPresetJars() {
        var resolver = new PathMatchingResourcePatternResolver();
        try {
            var resources = resolver.getResources(PRESETS_LOCATION_PATTERN);
            return Flux.fromArray(resources);
        } catch (IOException e) {
            return Flux.error(e);
        }
    }

    private Path toPath(Resource resource) {
        try {
            return Path.of(resource.getURI());
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
