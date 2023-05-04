package run.halo.app.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.SetupStateCache;

/**
 * Tests for {@link InitializeRedirectionWebFilter}.
 *
 * @author guqing
 * @since 2.5.2
 */
@ExtendWith(MockitoExtension.class)
class InitializeRedirectionWebFilterTest {

    @Mock
    private SetupStateCache setupStateCache;

    @Mock
    private ServerRedirectStrategy serverRedirectStrategy;

    @InjectMocks
    private InitializeRedirectionWebFilter filter;

    @BeforeEach
    void setUp() {
        filter.setRedirectStrategy(serverRedirectStrategy);
    }

    @Test
    void shouldRedirectWhenSystemNotInitialized() {
        when(setupStateCache.get()).thenReturn(false);

        WebFilterChain chain = mock(WebFilterChain.class);

        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(serverRedirectStrategy.sendRedirect(any(), any())).thenReturn(Mono.empty().then());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
            .expectNextCount(0)
            .expectComplete()
            .verify();

        verify(serverRedirectStrategy).sendRedirect(eq(exchange), eq(URI.create("/console")));
        verify(chain, never()).filter(eq(exchange));
    }

    @Test
    void shouldNotRedirectWhenSystemInitialized() {
        when(setupStateCache.get()).thenReturn(true);

        WebFilterChain chain = mock(WebFilterChain.class);

        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty().then());
        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
            .expectNextCount(0)
            .expectComplete()
            .verify();

        verify(serverRedirectStrategy, never()).sendRedirect(eq(exchange),
            eq(URI.create("/console")));
        verify(chain).filter(eq(exchange));
    }

    @Test
    void shouldNotRedirectWhenNotHomePage() {
        WebFilterChain chain = mock(WebFilterChain.class);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty().then());
        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
            .expectNextCount(0)
            .expectComplete()
            .verify();

        verify(serverRedirectStrategy, never()).sendRedirect(eq(exchange),
            eq(URI.create("/console")));
        verify(chain).filter(eq(exchange));
    }
}
