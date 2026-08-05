package run.halo.app.security.authentication.oauth2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;

@ExtendWith(MockitoExtension.class)
class DefaultOAuth2LoginHandlerEnhancerTest {

    @Mock
    UserConnectionService connectionService;

    @Mock
    OAuth2AuthenticationTokenCache tokenCache;

    @Mock
    OAuth2BindIntent bindIntent;

    MockServerWebExchange exchange;
    OAuth2AuthenticationToken oauth2Token;
    DefaultOAuth2LoginHandlerEnhancer enhancer;

    @BeforeEach
    void setUp() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/login"));
        var oauth2User = new DefaultOAuth2User(List.of(), Map.of("id", "external-user"), "id");
        oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");
        enhancer = new DefaultOAuth2LoginHandlerEnhancer(connectionService, tokenCache, bindIntent);
    }

    @Test
    void shouldBindOnlyAfterConsumingExplicitIntent() {
        var authentication = localAuthentication();
        when(bindIntent.consume(exchange)).thenReturn(Mono.just(true));
        when(tokenCache.getToken(exchange)).thenReturn(Mono.just(oauth2Token));
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.empty());
        when(connectionService.createUserConnection("halo-user", "github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.just(new UserConnection()));
        when(tokenCache.removeToken(exchange)).thenReturn(Mono.empty());

        enhancer.loginSuccess(exchange, authentication).as(StepVerifier::create).verifyComplete();

        verify(connectionService).createUserConnection("halo-user", "github", oauth2Token.getPrincipal());
        var order = inOrder(bindIntent, connectionService, tokenCache);
        order.verify(bindIntent).consume(exchange);
        order.verify(connectionService).updateUserConnectionIfPresent("github", oauth2Token.getPrincipal());
        order.verify(connectionService).createUserConnection("halo-user", "github", oauth2Token.getPrincipal());
        order.verify(tokenCache).removeToken(exchange);
    }

    @Test
    void shouldClearStaleOAuth2TokenWithoutBindingForOrdinaryLogin() {
        when(bindIntent.consume(exchange)).thenReturn(Mono.just(false));
        when(tokenCache.removeToken(exchange)).thenReturn(Mono.empty());

        enhancer.loginSuccess(exchange, localAuthentication())
                .as(StepVerifier::create)
                .verifyComplete();

        verify(tokenCache).removeToken(exchange);
        verify(tokenCache, never()).getToken(exchange);
        verify(connectionService, never()).createUserConnection(any(), any(), any());
    }

    @Test
    void shouldConsumeIntentOnceAndNotBindAgain() {
        when(bindIntent.consume(exchange)).thenReturn(Mono.just(true), Mono.just(false));
        when(tokenCache.getToken(exchange)).thenReturn(Mono.just(oauth2Token));
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.empty());
        when(connectionService.createUserConnection("halo-user", "github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.just(new UserConnection()));
        when(tokenCache.removeToken(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(enhancer.loginSuccess(exchange, localAuthentication())
                        .then(enhancer.loginSuccess(exchange, localAuthentication())))
                .verifyComplete();

        verify(connectionService).createUserConnection("halo-user", "github", oauth2Token.getPrincipal());
    }

    @Test
    void shouldClearCachedTokenWhenBindingFails() {
        var failure = new IllegalStateException("binding failed");
        when(bindIntent.consume(exchange)).thenReturn(Mono.just(true));
        when(tokenCache.getToken(exchange)).thenReturn(Mono.just(oauth2Token));
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.error(failure));
        when(tokenCache.removeToken(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(enhancer.loginSuccess(exchange, localAuthentication()))
                .expectErrorSatisfies(error ->
                        org.assertj.core.api.Assertions.assertThat(error).isSameAs(failure))
                .verify();

        verify(tokenCache).removeToken(exchange);
        verify(connectionService, never()).createUserConnection(any(), any(), any());
    }

    private static UsernamePasswordAuthenticationToken localAuthentication() {
        return UsernamePasswordAuthenticationToken.authenticated("halo-user", "password", List.of());
    }
}
