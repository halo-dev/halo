package run.halo.app.theme;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting.Theme;
import run.halo.app.infra.ThemeRootGetter;

/**
 * @author johnniang
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class ThemeResolver {

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private final ThemeRootGetter themeRoot;

    public Mono<ThemeContext> getThemeContext(String themeName) {
        Assert.hasText(themeName, "Theme name cannot be empty");
        var path = themeRoot.get().resolve(themeName);
        return Mono.just(ThemeContext.builder().name(themeName).path(path))
            .flatMap(builder -> environmentFetcher.fetch(Theme.GROUP, Theme.class)
                .mapNotNull(Theme::getActive)
                .map(activatedTheme -> {
                    boolean active = StringUtils.equals(activatedTheme, themeName);
                    return builder.active(active);
                })
                .defaultIfEmpty(builder.active(false))
            )
            .map(ThemeContext.ThemeContextBuilder::build);
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
                boolean active = StringUtils.equals(activatedTheme, themeName);
                var path = themeRoot.get().resolve(themeName);
                return builder.name(themeName)
                    .path(path)
                    .active(active)
                    .build();
            });
    }

}
