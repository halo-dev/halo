package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

class WebSessionOAuth2BindIntentTest {

    private static final Instant NOW = Instant.parse("2026-08-05T00:00:00Z");
    private static final String OAUTH2_IDENTITY = "oauth2-identity";

    MockServerWebExchange exchange;
    WebSessionOAuth2BindIntent bindIntent;

    @BeforeEach
    void setUp() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login?oauth2_bind"));
        bindIntent = new WebSessionOAuth2BindIntent();
        bindIntent.setClock(Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void shouldConsumeSavedIntentOnlyOnce() {
        StepVerifier.create(bindIntent
                        .save(exchange, OAUTH2_IDENTITY)
                        .then(bindIntent.consume(exchange))
                        .flatMap(first -> bindIntent
                                .consume(exchange)
                                .defaultIfEmpty("missing")
                                .map(second -> reactor.util.function.Tuples.of(first, second))))
                .assertNext(results -> {
                    assertThat(results.getT1()).isEqualTo(OAUTH2_IDENTITY);
                    assertThat(results.getT2()).isEqualTo("missing");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectAndRemoveExpiredIntent() {
        StepVerifier.create(bindIntent.save(exchange, OAUTH2_IDENTITY)).verifyComplete();
        bindIntent.setClock(Clock.fixed(NOW.plus(Duration.ofMinutes(6)), ZoneOffset.UTC));

        StepVerifier.create(bindIntent.consume(exchange)).verifyComplete();
        StepVerifier.create(bindIntent.consume(exchange)).verifyComplete();
    }
}
