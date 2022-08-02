package run.halo.app.theme;

import java.util.function.Function;
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
    private Function<ServerHttpRequest, ThemeContext> themeContextFunction;
    private final HaloProperties haloProperties;

    public ThemeResolver(SystemConfigurableEnvironmentFetcher environmentFetcher,
        HaloProperties haloProperties) {
        this.environmentFetcher = environmentFetcher;
        this.haloProperties = haloProperties;
        themeContextFunction = this::defaultThemeContextFunction;
    }

    public ThemeContext getTheme(ServerHttpRequest request) {
        return themeContextFunction.apply(request);
    }

    private ThemeContext defaultThemeContextFunction(ServerHttpRequest request) {
        var builder = ThemeContext.builder();
        var themeName = request.getQueryParams().getFirst(ThemeContext.THEME_PREVIEW_PARAM_NAME);
        // TODO Fetch activated theme name from other place.
        String activation = environmentFetcher.fetch(Theme.GROUP, Theme.class)
            .map(Theme::getActive)
            .orElseThrow();
        if (StringUtils.isBlank(themeName)) {
            themeName = activation;
        }
        if (StringUtils.equals(activation, themeName)) {
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

    public Function<ServerHttpRequest, ThemeContext> getThemeContextFunction() {
        return themeContextFunction;
    }

    public void setThemeContextFunction(
        Function<ServerHttpRequest, ThemeContext> themeContextFunction) {
        this.themeContextFunction = themeContextFunction;
    }
}
