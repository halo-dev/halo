package run.halo.app.theme;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.theme.router.ModelConst;

/**
 * Provides template variables related to preview mode.
 */
@Component
public class PreviewContextVariablesAcquirer implements ViewContextBasedVariablesAcquirer {

    @Override
    public Mono<Map<String, Object>> acquire(ServerWebExchange exchange) {
        Boolean preview = (Boolean) exchange.getAttribute(ModelConst.IS_PREVIEW);
        if (Boolean.TRUE.equals(preview)) {
            return Mono.just(Map.of("isPreview", true));
        }
        return Mono.just(Map.of());
    }
} 