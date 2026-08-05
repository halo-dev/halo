package run.halo.app.security.authentication.oauth2;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class WebSessionOAuth2BindIntent implements OAuth2BindIntent {

    private static final String SESSION_ATTRIBUTE_KEY = OAuth2BindIntent.class + ".CREATED_AT";
    private static final Duration MAX_AGE = Duration.ofMinutes(5);

    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> save(ServerWebExchange exchange) {
        return exchange.getSession()
                .doOnNext(session -> session.getAttributes().put(SESSION_ATTRIBUTE_KEY, clock.instant()))
                .then();
    }

    @Override
    public Mono<Boolean> consume(ServerWebExchange exchange) {
        return exchange.getSession().map(session -> {
            var createdAt = session.getAttributes().remove(SESSION_ATTRIBUTE_KEY);
            return createdAt instanceof Instant instant
                    && !instant.plus(MAX_AGE).isBefore(clock.instant());
        });
    }

    @Override
    public Mono<Void> clear(ServerWebExchange exchange) {
        return exchange.getSession()
                .doOnNext(session -> session.getAttributes().remove(SESSION_ATTRIBUTE_KEY))
                .then();
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }
}
