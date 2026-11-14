package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;

@ExtendWith(MockitoExtension.class)
class MapOAuth2AuthenticationFilterTest {

    @Mock
    ServerSecurityContextRepository securityContextRepository;

    @Mock
    UserConnectionService connectionService;

    @Mock
    OAuth2AuthenticationSession authenticationSession;

    @Mock
    OAuth2AuthenticationTokenCache tokenCache;

    MockServerWebExchange exchange;

    WebFilterChain chain;

    MapOAuth2AuthenticationFilter filter;

    OAuth2AuthenticationToken oauth2Token;

    @BeforeEach
    void setUp() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login/oauth2/code/github"));
        chain = ignored -> Mono.empty();
        filter = new MapOAuth2AuthenticationFilter(
                securityContextRepository, connectionService, authenticationSession, tokenCache);
        oauth2Token = oauth2Token();
    }

    @Test
    void shouldCacheUnboundTokenAndRedirectToSelection() {
        when(securityContextRepository.load(exchange)).thenReturn(Mono.empty());
        when(securityContextRepository.save(exchange, null)).thenReturn(Mono.empty());
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.empty());
        when(tokenCache.saveToken(exchange, oauth2Token)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(oauth2Token))
                .as(StepVerifier::create)
                .verifyComplete();

        verify(tokenCache).saveToken(exchange, oauth2Token);
        verify(authenticationSession, never()).establish(any(), anyString(), any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/login?oauth2_select"));
    }

    @Test
    void shouldEstablishSessionForExistingConnection() {
        var connection = connection("connected-user");
        when(securityContextRepository.load(exchange)).thenReturn(Mono.empty());
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.just(connection));
        when(authenticationSession.establish(exchange, "connected-user", oauth2Token))
                .thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(oauth2Token))
                .as(StepVerifier::create)
                .verifyComplete();

        verify(authenticationSession).establish(exchange, "connected-user", oauth2Token);
        verify(tokenCache, never()).saveToken(any(), any());
    }

    @Test
    void shouldBindExistingHaloUserAndEstablishSession() {
        var haloAuthentication = UsernamePasswordAuthenticationToken.authenticated("halo-user", "", List.of());
        var connection = connection("halo-user");
        when(securityContextRepository.load(exchange)).thenReturn(Mono.just(new SecurityContextImpl(oauth2Token)));
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.empty());
        when(connectionService.createUserConnection("halo-user", "github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.just(connection));
        when(authenticationSession.establish(exchange, "halo-user", oauth2Token))
                .thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(haloAuthentication))
                .as(StepVerifier::create)
                .verifyComplete();

        verify(connectionService).createUserConnection("halo-user", "github", oauth2Token.getPrincipal());
        verify(authenticationSession).establish(exchange, "halo-user", oauth2Token);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldNotTreatRawOAuth2AuthenticationAsExistingHaloUser() {
        when(securityContextRepository.load(exchange)).thenReturn(Mono.just(new SecurityContextImpl(oauth2Token)));
        when(securityContextRepository.save(exchange, null)).thenReturn(Mono.empty());
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.empty());
        when(tokenCache.saveToken(exchange, oauth2Token)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(oauth2Token))
                .as(StepVerifier::create)
                .verifyComplete();

        verify(connectionService, never()).createUserConnection(anyString(), anyString(), any());
        verify(tokenCache).saveToken(exchange, oauth2Token);
        verify(authenticationSession, never()).establish(any(), anyString(), any());
    }

    private static OAuth2AuthenticationToken oauth2Token() {
        var principal = new DefaultOAuth2User(List.of(), Map.of("id", "oauth-user"), "id");
        return new OAuth2AuthenticationToken(principal, List.of(), "github");
    }

    private static UserConnection connection(String username) {
        var connection = new UserConnection();
        var spec = new UserConnection.UserConnectionSpec();
        spec.setUsername(username);
        connection.setSpec(spec);
        return connection;
    }
}
