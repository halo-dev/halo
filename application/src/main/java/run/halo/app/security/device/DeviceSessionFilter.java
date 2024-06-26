package run.halo.app.security.device;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DeviceSessionFilter implements WebFilter {
    private final DeviceService deviceService;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return exchange.getSession()
            .flatMap(session -> deviceService.changeSessionId(exchange))
            .then(chain.filter(exchange));
    }
}
