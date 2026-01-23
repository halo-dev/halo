package run.halo.app.security.device;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
class DeviceSessionFilter implements WebFilter {

    private final DeviceService deviceService;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return exchange.getSession().flatMap(session -> {
            var previousId = session.getId();
            return chain.filter(exchange)
                .then(Mono.defer(() -> {
                    var currentId = session.getId();
                    if (Objects.equals(previousId, currentId)) {
                        return Mono.empty();
                    }
                    // only when session id changed
                    log.debug("Session ID changed from {} to {}, updating device info.",
                        previousId, currentId);
                    return deviceService.changeSessionId(exchange);
                }));
        });
    }
}
