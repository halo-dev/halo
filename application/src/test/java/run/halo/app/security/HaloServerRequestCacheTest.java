package run.halo.app.security;

import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.session.DefaultWebSessionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class HaloServerRequestCacheTest {

    HaloServerRequestCache requestCache;

    @BeforeEach
    void setUp() {
        requestCache = new HaloServerRequestCache();
    }

    @Test
    void shouldNotSaveIfPageNotCacheable() {
        var mockExchange =
            MockServerWebExchange.from(MockServerHttpRequest.get("/login"));
        requestCache.saveRequest(mockExchange)
            .then(requestCache.getRedirectUri(mockExchange))
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void shouldSaveIfPageCacheable() {
        var mockExchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML)
        );
        requestCache.saveRequest(mockExchange)
            .then(requestCache.getRedirectUri(mockExchange))
            .as(StepVerifier::create)
            .expectNext(URI.create("/archives"))
            .verifyComplete();
    }

    @Test
    void shouldSaveIfQueryPresent() {
        var mockExchange =
            MockServerWebExchange.from(MockServerHttpRequest.get("/login?redirect_uri=/halo?q=v"));
        requestCache.saveRequest(mockExchange)
            .then(requestCache.getRedirectUri(mockExchange))
            .as(StepVerifier::create)
            .expectNext(URI.create("/halo?q=v"));
    }

    @Test
    void shouldRemoveIfRedirectUriFound() {
        var sessionManager = new DefaultWebSessionManager();
        var mockExchange =
            MockServerWebExchange.builder(MockServerHttpRequest.get("/login?redirect_uri=/halo"))
                .sessionManager(sessionManager)
                .build();
        var removeExchange = mockExchange.mutate()
            .request(builder -> builder.uri(URI.create("/halo")))
            .build();
        requestCache.saveRequest(mockExchange)
            .then(Mono.defer(() -> requestCache.removeMatchingRequest(removeExchange)))
            .as(StepVerifier::create)
            .assertNext(request -> {
                Assertions.assertEquals(URI.create("/halo"), request.getURI());
            })
            .verifyComplete();
    }

}
