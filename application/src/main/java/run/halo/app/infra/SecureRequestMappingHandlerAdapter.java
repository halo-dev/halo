package run.halo.app.infra;

import org.springframework.lang.NonNull;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Secure request mapping handler adapter.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class SecureRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

    @Override
    @NonNull
    public Mono<HandlerResult> handle(
        @NonNull ServerWebExchange exchange,
        @NonNull Object handler
    ) {
        return super.handle(new SecureServerWebExchange(exchange), handler);
    }

}
