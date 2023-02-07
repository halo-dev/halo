package run.halo.app.theme;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.theme.finders.ThemeFinder;

/**
 * Theme context based variables acquirer.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ThemeContextBasedVariablesAcquirer implements ViewContextBasedVariablesAcquirer {
    private final ThemeFinder themeFinder;
    private final ThemeResolver themeResolver;

    public ThemeContextBasedVariablesAcquirer(ThemeFinder themeFinder,
        ThemeResolver themeResolver) {
        this.themeFinder = themeFinder;
        this.themeResolver = themeResolver;
    }

    @Override
    public Mono<Map<String, Object>> acquire(ServerWebExchange exchange) {
        return themeResolver.getTheme(exchange)
            .flatMap(themeContext -> {
                String name = themeContext.getName();
                return themeFinder.getByName(name);
            })
            .map(themeVo -> Map.<String, Object>of("theme", themeVo))
            .defaultIfEmpty(Map.of());
    }
}
