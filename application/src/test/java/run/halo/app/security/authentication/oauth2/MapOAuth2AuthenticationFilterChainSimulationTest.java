package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.security.LoginHandlerEnhancer;

@ExtendWith(MockitoExtension.class)
class MapOAuth2AuthenticationFilterChainSimulationTest {

    @Mock
    UserConnectionService connectionService;

    @Mock
    ReactiveUserDetailsService userDetailsService;

    @Mock
    LoginHandlerEnhancer loginHandlerEnhancer;

    WebSessionServerSecurityContextRepository securityContextRepository;

    WebSessionOAuth2AuthenticationTokenCache tokenCache;

    OAuth2AuthenticationSession authenticationSession;

    OAuth2AuthenticationToken oauth2Token;

    @BeforeEach
    void setUp() {
        securityContextRepository = new WebSessionServerSecurityContextRepository();
        tokenCache = new WebSessionOAuth2AuthenticationTokenCache();
        authenticationSession =
                new OAuth2AuthenticationSession(userDetailsService, securityContextRepository, loginHandlerEnhancer);
        var principal = new DefaultOAuth2User(List.of(), Map.of("id", "oauth-user"), "id");
        oauth2Token = new OAuth2AuthenticationToken(principal, List.of(), "github");
    }

    @Test
    void shouldClearAnonymousSessionAuthenticationAndCacheTokenForSelection() {
        var exchange = exchange();
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.empty());
        var filter = filter();

        filter.filter(exchange, downstreamOAuth2LoginChain())
                .as(StepVerifier::create)
                .verifyComplete();

        securityContextRepository.load(exchange).as(StepVerifier::create).verifyComplete();
        tokenCache
                .getToken(exchange)
                .as(StepVerifier::create)
                .expectNext(oauth2Token)
                .verifyComplete();
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/login?oauth2_select"));
        verify(userDetailsService, never()).findByUsername(anyString());
    }

    @Test
    void shouldReplaceDownstreamOAuth2AuthenticationWithBoundHaloAuthentication() {
        var exchange = exchange();
        var filter = filter();
        var preAuthentication = UsernamePasswordAuthenticationToken.authenticated("halo-user", "", List.of());
        var userDetails = User.withUsername("halo-user")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        var connection = connection("halo-user");
        when(connectionService.updateUserConnectionIfPresent("github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.empty());
        when(connectionService.createUserConnection("halo-user", "github", oauth2Token.getPrincipal()))
                .thenReturn(Mono.just(connection));
        when(userDetailsService.findByUsername("halo-user")).thenReturn(Mono.just(userDetails));
        when(loginHandlerEnhancer.onLoginSuccess(eq(exchange), any())).thenReturn(Mono.empty());

        filter.filter(exchange, downstreamOAuth2LoginChain())
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(preAuthentication))
                .as(StepVerifier::create)
                .verifyComplete();

        securityContextRepository
                .load(exchange)
                .map(context -> context.getAuthentication())
                .as(StepVerifier::create)
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(HaloOAuth2AuthenticationToken.class);
                    assertThat(authentication.getName()).isEqualTo("halo-user");
                    assertThat(((HaloOAuth2AuthenticationToken) authentication).getOriginal())
                            .isSameAs(oauth2Token);
                })
                .verifyComplete();
        tokenCache.getToken(exchange).as(StepVerifier::create).verifyComplete();
    }

    private MapOAuth2AuthenticationFilter filter() {
        return new MapOAuth2AuthenticationFilter(
                securityContextRepository, connectionService, authenticationSession, tokenCache);
    }

    private WebFilterChain downstreamOAuth2LoginChain() {
        WebFilter oauth2LoginFilter =
                (exchange, chain) -> securityContextRepository.save(exchange, new SecurityContextImpl(oauth2Token));
        return exchange -> oauth2LoginFilter.filter(exchange, ignored -> Mono.empty());
    }

    private static MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/login/oauth2/code/github"));
    }

    private static UserConnection connection(String username) {
        var connection = new UserConnection();
        var spec = new UserConnection.UserConnectionSpec();
        spec.setUsername(username);
        connection.setSpec(spec);
        return connection;
    }
}
