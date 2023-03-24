package run.halo.app.console;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

class WebSocketServerWebExchangeMatcherTest {

    @Test
    void shouldMatchIfWebSocketProtocol() {
        var httpRequest = MockServerHttpRequest.get("")
            .header(HttpHeaders.CONNECTION, HttpHeaders.UPGRADE)
            .header(HttpHeaders.UPGRADE, "websocket")
            .build();
        var wsExchange = MockServerWebExchange.from(httpRequest);
        var wsMatcher = new WebSocketServerWebExchangeMatcher();
        StepVerifier.create(wsMatcher.matches(wsExchange))
            .consumeNextWith(result -> assertTrue(result.isMatch()))
            .verifyComplete();
    }

    @Test
    void shouldNotMatchIfNotWebSocketProtocol() {
        var httpRequest = MockServerHttpRequest.get("")
            .header(HttpHeaders.CONNECTION, HttpHeaders.UPGRADE)
            .header(HttpHeaders.UPGRADE, "not-a-websocket")
            .build();
        var wsExchange = MockServerWebExchange.from(httpRequest);
        var wsMatcher = new WebSocketServerWebExchangeMatcher();
        StepVerifier.create(wsMatcher.matches(wsExchange))
            .consumeNextWith(result -> assertFalse(result.isMatch()))
            .verifyComplete();
    }
}