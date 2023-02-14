package run.halo.app.theme.router;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveView;
import reactor.core.publisher.Mono;

/**
 * Empty view for test.
 *
 * @author guqing
 * @since 2.0.0
 */
public class EmptyView extends ThymeleafReactiveView {
    public EmptyView() {
    }

    @Override
    public Mono<Void> render(Map<String, ?> model, MediaType contentType,
        ServerWebExchange exchange) {
        return Mono.empty();
    }
}
