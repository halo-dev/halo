package run.halo.app.theme.service;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.SystemSetting;

public interface ThemeService {

    Mono<Void> installPresetTheme();

    Mono<Theme> install(Publisher<DataBuffer> content);

    Mono<Theme> upgrade(String themeName, Publisher<DataBuffer> content);

    Mono<Theme> reloadTheme(String name);

    Mono<ConfigMap> resetSettingConfig(String name);

    /**
     * Fetch activated theme.
     *
     * @return the activated theme
     */
    Mono<Theme> fetchActivatedTheme();

    /**
     * Fetch system setting of theme.
     *
     * @return the system setting of theme
     */
    Mono<SystemSetting.Theme> fetchSystemSetting();

    /**
     * Fetch activated theme name.
     *
     * @return the activated theme name
     */
    default Mono<String> fetchActivatedThemeName() {
        return fetchSystemSetting()
            .mapNotNull(SystemSetting.Theme::getActive)
            .filter(StringUtils::hasText);
    }

    // TODO Migrate other useful methods in ThemeEndpoint in the future.
}
