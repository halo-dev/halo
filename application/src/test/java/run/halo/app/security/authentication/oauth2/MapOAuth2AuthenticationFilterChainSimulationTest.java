package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.security.LoginHandlerEnhancer;

/**
 * Simulates the real security chain to verify {@link MapOAuth2AuthenticationFilter} behavior:
 *
 * <pre>
 * [session context loaded by ReactorContextWebFilter]
 *   -&gt; MapOAuth2AuthenticationFilter
 *   -&gt; a mock OAuth2 login filter mimicking {@code AuthenticationWebFilter} (saves the raw
 *      OAuth2AuthenticationToken into the session and short-circuits with a redirect)
 * </pre>
 *
 * The post-chain read must come from the session repository, not the reactive context, because the OAuth2 filter's
 * {@code contextWrite} only scopes its own downstream.
 */
class MapOAuth2AuthenticationFilterChainSimulationTest {

    DefaultOAuth2User oauth2User() {
        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("sub", "alice"), "sub");
    }

    UserConnection connection(String username) {
        var connection = new UserConnection();
        connection.setMetadata(new run.halo.app.extension.Metadata());
        var spec = new UserConnection.UserConnectionSpec();
        spec.setUsername(username);
        connection.setSpec(spec);
        return connection;
    }

    /**
     * A logged-in user binds a new OAuth2 account: the session must end up authenticated as the logged-in user
     * (HaloOAuth2AuthenticationToken) and the connection must be created for them.
     */
    @Test
    void shouldBindToLoggedInUserAndKeepHaloAuthentication() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/oauth2/callback"));
        var sessionRepository = new WebSessionServerSecurityContextRepository();

        var loggedInContext = new SecurityContextImpl(new UsernamePasswordAuthenticationToken(
                "existing-user", "pw", List.of(new SimpleGrantedAuthority("ROLE_authenticated"))));
        sessionRepository.save(exchange, loggedInContext).block();

        var oauth2User = oauth2User();
        var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        var connectionService = mock(UserConnectionService.class);
        when(connectionService.updateUserConnectionIfPresent("github", oauth2User))
                .thenReturn(Mono.empty());
        when(connectionService.createUserConnection(any(), any(), any()))
                .thenReturn(Mono.just(connection("existing-user")));
        var userDetails = User.withUsername("existing-user")
                .password("")
                .roles("authenticated")
                .build();
        var userDetailsService = mock(ReactiveUserDetailsService.class);
        when(userDetailsService.findByUsername("existing-user")).thenReturn(Mono.just(userDetails));
        var loginHandlerEnhancer = mock(LoginHandlerEnhancer.class);
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());
        var tokenCache = mock(OAuth2AuthenticationTokenCache.class);

        var mapFilter = new MapOAuth2AuthenticationFilter(
                sessionRepository, connectionService, userDetailsService, loginHandlerEnhancer);
        mapFilter.setAuthenticationCache(tokenCache);

        WebFilter mockOAuth2LoginFilter = (requestExchange, chain) -> {
            var context = new SecurityContextImpl(oauth2Token);
            return sessionRepository
                    .save(requestExchange, context)
                    .then(Mono.defer(() -> {
                        requestExchange.getResponse().setStatusCode(HttpStatus.FOUND);
                        requestExchange.getResponse().getHeaders().setLocation(URI.create("/uc"));
                        return requestExchange.getResponse().setComplete();
                    }))
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
        };

        var chain = new SimpleWebFilterChain(List.of(mapFilter, mockOAuth2LoginFilter));
        var contextLoader = (WebFilter) (requestExchange, filterChain) -> sessionRepository
                .load(requestExchange)
                .defaultIfEmpty(new SecurityContextImpl())
                .flatMap(context -> filterChain
                        .filter(requestExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))));

        contextLoader.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/uc"));
        verify(connectionService).createUserConnection(eq("existing-user"), eq("github"), any());
        verify(tokenCache, never()).saveToken(any(), any());

        var sessionAuth = sessionRepository
                .load(exchange)
                .map(SecurityContext::getAuthentication)
                .block();
        assertThat(sessionAuth).isInstanceOf(HaloOAuth2AuthenticationToken.class);
        assertThat(sessionAuth.getName()).isEqualTo("existing-user");
    }

    /**
     * An anonymous user without a connection is redirected to the selection page and the session context is cleared
     * (the OAuth2 token is cached for later registration/binding).
     */
    @Test
    void shouldRedirectAnonymousUserToSelectPage() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/oauth2/callback"));
        var sessionRepository = new WebSessionServerSecurityContextRepository();

        var oauth2User = oauth2User();
        var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        var connectionService = mock(UserConnectionService.class);
        when(connectionService.updateUserConnectionIfPresent("github", oauth2User))
                .thenReturn(Mono.empty());
        var userDetailsService = mock(ReactiveUserDetailsService.class);
        var loginHandlerEnhancer = mock(LoginHandlerEnhancer.class);
        var tokenCache = mock(OAuth2AuthenticationTokenCache.class);
        when(tokenCache.saveToken(any(), any())).thenReturn(Mono.empty());

        var mapFilter = new MapOAuth2AuthenticationFilter(
                sessionRepository, connectionService, userDetailsService, loginHandlerEnhancer);
        mapFilter.setAuthenticationCache(tokenCache);

        WebFilter mockOAuth2LoginFilter = (requestExchange, chain) -> {
            var context = new SecurityContextImpl(oauth2Token);
            return sessionRepository
                    .save(requestExchange, context)
                    .then(Mono.defer(() -> {
                        requestExchange.getResponse().setStatusCode(HttpStatus.FOUND);
                        requestExchange.getResponse().getHeaders().setLocation(URI.create("/uc"));
                        return Mono.<Void>empty();
                    }))
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
        };

        var chain = new SimpleWebFilterChain(List.of(mapFilter, mockOAuth2LoginFilter));
        var contextLoader = (WebFilter) (requestExchange, filterChain) -> sessionRepository
                .load(requestExchange)
                .defaultIfEmpty(new SecurityContextImpl())
                .flatMap(context -> filterChain
                        .filter(requestExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))));

        contextLoader.filter(exchange, chain).block();

        verify(connectionService, never()).createUserConnection(any(), any(), any());
        verify(tokenCache).saveToken(eq(exchange), eq(oauth2Token));
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/login?oauth2_select"));
    }

    /** A minimal WebFilterChain running filters in order. */
    static class SimpleWebFilterChain implements WebFilterChain {
        private final List<WebFilter> filters;
        private int index = 0;

        SimpleWebFilterChain(List<WebFilter> filters) {
            this.filters = filters;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            if (index < filters.size()) {
                var filter = filters.get(index++);
                return filter.filter(exchange, this);
            }
            return Mono.empty();
        }
    }
}
