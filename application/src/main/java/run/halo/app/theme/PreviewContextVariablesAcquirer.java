package run.halo.app.theme;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.theme.router.ModelConst;

/**
 * Provides template variables related to preview mode.
 * This acquirer checks if the current request is in preview mode and provides
 * the corresponding template variable.
 *
 * @author humphreyLi
 * @since 2.6.0
 */
@Component
public class PreviewContextVariablesAcquirer implements ViewContextBasedVariablesAcquirer {

    /**
     * Acquires template variables for preview mode.
     *
     * @param exchange the server web exchange
     * @return a Mono containing template variables map
     */
    @Override
    public Mono<Map<String, Object>> acquire(ServerWebExchange exchange) {
        Boolean preview = (Boolean) exchange.getAttribute(ModelConst.IS_PREVIEW);
        if (Boolean.TRUE.equals(preview)) {
            return Mono.just(Map.of("isPreview", true));
        }
        return Mono.just(Map.of());
    }
} 