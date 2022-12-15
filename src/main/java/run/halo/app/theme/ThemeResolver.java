package run.halo.app.theme;

import java.nio.file.Files;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
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
@AllArgsConstructor
public class ThemeResolver {
    private static final String THEME_WORK_DIR = "themes";
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private final HaloProperties haloProperties;

    private final ThymeleafProperties thymeleafProperties;

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
                var path = FilePathUtils.combinePath(haloProperties.getWorkDir().toString(),
                    THEME_WORK_DIR, themeName);
                return builder.name(themeName)
                    .path(path)
                    .active(active)
                    .build();
            });
    }

    /**
     * Check whether the template file exists.
     *
     * @param viewName view name must not be blank
     * @return if exists return true, otherwise return false
     */
    public Mono<Boolean> isTemplateAvailable(ServerHttpRequest request, String viewName) {
        return getTheme(request)
            .map(themeContext -> {
                String prefix = themeContext.getPath() + "/templates/";
                String viewNameToUse = viewName;
                if (!viewNameToUse.endsWith(thymeleafProperties.getSuffix())) {
                    viewNameToUse = viewNameToUse + thymeleafProperties.getSuffix();
                }
                return Files.exists(FilePathUtils.combinePath(prefix, viewNameToUse));
            })
            .onErrorResume(e -> Mono.just(false));
    }
}
