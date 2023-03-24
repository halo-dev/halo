package run.halo.app.security.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

class WebExchangeMatchersTest {

    @Test
    void shouldNotMatchMediaTypeAll() {
        assertion(Set.of(APPLICATION_JSON), Set.of(APPLICATION_JSON, ALL), true);
        assertion(Set.of(APPLICATION_JSON), Set.of(ALL), false);
        assertion(Set.of(APPLICATION_JSON), Set.of(APPLICATION_JSON), true);
        assertion(Set.of(APPLICATION_JSON), Set.of(APPLICATION_JSON, TEXT_HTML), true);
    }

    void assertion(Set<MediaType> matchingMediaTypes,
        Set<MediaType> acceptMediaTypes,
        boolean expectMatch) {
        var matcher = ignoringMediaTypeAll(matchingMediaTypes.toArray(new MediaType[0]));
        MockServerHttpRequest request = MockServerHttpRequest.get("/fake")
            .accept(acceptMediaTypes.toArray(new MediaType[0]))
            .build();
        var webExchange = MockServerWebExchange.from(request);
        StepVerifier.create(matcher.matches(webExchange))
            .consumeNextWith(matchResult -> assertEquals(expectMatch, matchResult.isMatch()))
            .verifyComplete();
    }
}