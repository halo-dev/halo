package run.halo.app.theme;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.theme.finders.ThemeFinder;
import run.halo.app.theme.finders.vo.ThemeVo;

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
        return themeResolver.getTheme(exchange.getRequest())
            .publishOn(Schedulers.boundedElastic())
            .map(themeContext -> {
                String name = themeContext.getName();
                ThemeVo themeVo = themeFinder.getByName(name);
                if (themeVo == null) {
                    return Map.of();
                }
                return Map.of("theme", themeVo);
            });
    }
}
