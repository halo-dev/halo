package run.halo.app.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.InitializationStateGetter;

/**
 * Tests for {@link InitializeRedirectionWebFilter}.
 *
 * @author guqing
 * @since 2.5.2
 */
@ExtendWith(MockitoExtension.class)
class InitializeRedirectionWebFilterTest {

    @Mock
    private InitializationStateGetter initializationStateGetter;

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
        when(initializationStateGetter.userInitialized()).thenReturn(Mono.just(false));

        WebFilterChain chain = mock(WebFilterChain.class);
        var paths = new String[] {"/", "/console/test", "/uc/test", "/login", "/signup"};
        for (String path : paths) {
            MockServerHttpRequest request = MockServerHttpRequest.get(path)
                .accept(MediaType.TEXT_HTML).build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            when(serverRedirectStrategy.sendRedirect(any(), any())).thenReturn(Mono.empty().then());

            Mono<Void> result = filter.filter(exchange, chain);

            StepVerifier.create(result)
                .expectNextCount(0)
                .expectComplete()
                .verify();

            verify(serverRedirectStrategy).sendRedirect(eq(exchange),
                eq(URI.create("/system/setup")));
            verify(chain, never()).filter(eq(exchange));
        }
    }

    @Test
    void shouldNotRedirectWhenSystemInitialized() {
        lenient().when(initializationStateGetter.userInitialized()).thenReturn(Mono.just(true));

        WebFilterChain chain = mock(WebFilterChain.class);

        var paths = new String[] {"/test", "/apis/test", "system/setup", "/logout"};
        for (String path : paths) {
            MockServerHttpRequest request = MockServerHttpRequest.get(path)
                .accept(MediaType.TEXT_HTML).build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            when(chain.filter(any())).thenReturn(Mono.empty().then());
            Mono<Void> result = filter.filter(exchange, chain);

            StepVerifier.create(result)
                .expectNextCount(0)
                .expectComplete()
                .verify();

            verify(serverRedirectStrategy, never()).sendRedirect(eq(exchange), any());
            verify(chain).filter(eq(exchange));
        }
    }

    @Test
    void shouldNotRedirectTest() {
        WebFilterChain chain = mock(WebFilterChain.class);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
            .accept(MediaType.TEXT_HTML).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty().then());
        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
            .expectNextCount(0)
            .expectComplete()
            .verify();

        verify(serverRedirectStrategy, never()).sendRedirect(eq(exchange), any());
        verify(chain).filter(eq(exchange));
    }
}
