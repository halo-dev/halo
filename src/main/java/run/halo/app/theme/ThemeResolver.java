package run.halo.app.theme;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
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

    public ThemeContext getTheme(ServerHttpRequest request) {
        var builder = ThemeContext.builder();
        var themeName = request.getQueryParams().getFirst(ThemeContext.THEME_PREVIEW_PARAM_NAME);
        if (StringUtils.isBlank(themeName)) {
            // TODO Fetch activated theme name from other place.
            themeName = environmentFetcher.fetch(Theme.GROUP, Theme.class)
                .map(Theme::getActive)
                // It will never happen
                .orElse("default");
            builder.active(true);
        }
        // TODO Validate the existence of the theme name.

        var path = FilePathUtils.combinePath(haloProperties.getWorkDir().toString(),
            THEME_WORK_DIR, themeName);

        return builder
            .name(themeName)
            .path(path)
            .build();
    }
}
