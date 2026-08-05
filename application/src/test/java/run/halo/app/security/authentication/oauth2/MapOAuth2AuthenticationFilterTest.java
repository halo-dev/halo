package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.security.LoginHandlerEnhancer;

class MapOAuth2AuthenticationFilterTest {

    @Test
    void shouldRedirectToSelectPageWhenNotBoundAndNotLoggedIn() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/oauth2/callback"));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        var user = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_authenticated")), Map.of("sub", "alice"), "sub");
        var oauth2Token = new OAuth2AuthenticationToken(user, List.of(), "github");

        var connectionService = mock(UserConnectionService.class);
        when(connectionService.updateUserConnectionIfPresent("github", user)).thenReturn(Mono.empty());
        when(connectionService.createUserConnection(any(), any(), any())).thenReturn(Mono.empty());
        var securityContextRepository = mock(ServerSecurityContextRepository.class);
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(securityContextRepository.load(any())).thenReturn(Mono.empty());
        var userDetailsService = mock(ReactiveUserDetailsService.class);
        var loginHandlerEnhancer = mock(LoginHandlerEnhancer.class);
        var filter = new MapOAuth2AuthenticationFilter(
                securityContextRepository, connectionService, userDetailsService, loginHandlerEnhancer);
        var tokenCache = mock(OAuth2AuthenticationTokenCache.class);
        when(tokenCache.saveToken(eq(exchange), any())).thenReturn(Mono.empty());
        filter.setAuthenticationCache(tokenCache);

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(oauth2Token))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/login?oauth2_select"));
    }
}
