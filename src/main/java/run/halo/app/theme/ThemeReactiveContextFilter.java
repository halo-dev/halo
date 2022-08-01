package run.halo.app.theme;

import java.nio.file.Path;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.properties.HaloProperties;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ThemeReactiveContextFilter implements WebFilter {
    private static final String THEME_LOCATION = "themes";
    private static final String THEME_PARAMETER = "theme";
    private final SystemConfigurableEnvironmentFetcher systemConfigurableEnvironmentFetcher;
    private final Path workDir;
    private BiFunction<ServerWebExchange, Context, Context> themeContextFunction =
        this::defaultThemeContext;

    public ThemeReactiveContextFilter(
        SystemConfigurableEnvironmentFetcher systemConfigurableEnvironmentFetcher,
        HaloProperties haloProperties) {
        this.systemConfigurableEnvironmentFetcher = systemConfigurableEnvironmentFetcher;
        this.workDir = haloProperties.getWorkDir();
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
            .contextWrite(context -> themeContextFunction.apply(exchange, context));
    }

    public BiFunction<ServerWebExchange, Context, Context> getThemeContextFunction() {
        return themeContextFunction;
    }

    public void setThemeContextFunction(
        BiFunction<ServerWebExchange, Context, Context> themeContextFunction) {
        this.themeContextFunction = themeContextFunction;
    }

    private ThemeContext buildThemeContext(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String themeName = request.getQueryParams().getFirst(THEME_PARAMETER);
        boolean isActive = false;
        if (themeName == null) {
            themeName = getActiveThemeName();
            isActive = true;
        }
        return ThemeContext.builder()
            .themeName(themeName)
            .isActive(isActive)
            .path(themePath(themeName))
            .build();
    }

    String getActiveThemeName() {
        SystemSetting.Theme theme = systemConfigurableEnvironmentFetcher.get(SystemSetting.THEME);
        if (theme == null) {
            return StringUtils.EMPTY;
        }
        return theme.getActive();
    }

    Path themePath(String themeName) {
        return workDir.resolve(THEME_LOCATION)
            .resolve(themeName);
    }

    final Context defaultThemeContext(ServerWebExchange exchange, Context context) {
        return context.put(ThemeContext.THEME_CONTEXT_KEY,
            buildThemeContext(exchange));
    }

}
