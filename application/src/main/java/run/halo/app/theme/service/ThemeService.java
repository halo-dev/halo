package run.halo.app.theme.service;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;

public interface ThemeService {

    Mono<Void> installPresetTheme();

    Mono<Theme> install(Publisher<DataBuffer> content);

    Mono<Theme> upgrade(String themeName, Publisher<DataBuffer> content);

    Mono<Theme> reloadTheme(String name);

    Mono<ConfigMap> resetSettingConfig(String name);
    // TODO Migrate other useful methods in ThemeEndpoint in the future.

}
