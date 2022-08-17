package run.halo.app.theme;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting.Theme;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FilePathUtils;

/**
 * @author johnniang
 * @since 2.0.0
 */
@Component
public class ThemeResolver {
    private static final String THEME_WORK_DIR = "themes";
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private final HaloProperties haloProperties;

    public ThemeResolver(SystemConfigurableEnvironmentFetcher environmentFetcher,
        HaloProperties haloProperties) {
        this.environmentFetcher = environmentFetcher;
        this.haloProperties = haloProperties;
    }

    public Mono<ThemeContext> getTheme(ServerHttpRequest request) {
        return environmentFetcher.fetch(Theme.GROUP, Theme.class)
            .map(Theme::getActive)
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("No theme activated")))
            .map(activatedTheme -> {
                var builder = ThemeContext.builder();
                var themeName =
                    request.getQueryParams().getFirst(ThemeContext.THEME_PREVIEW_PARAM_NAME);
                if (StringUtils.isBlank(themeName)) {
                    themeName = activatedTheme;
                }
                boolean active = false;
                if (StringUtils.equals(activatedTheme, themeName)) {
                    active = true;
                }
                var path = FilePathUtils.combinePath(haloProperties.getWorkDir().toString(),
                    THEME_WORK_DIR, themeName);
                return builder.name(themeName)
                    .path(path)
                    .active(active)
                    .build();
            });
    }

}
