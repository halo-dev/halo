package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.security.LoginHandlerEnhancer;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSessionTest {

    @Mock
    ReactiveUserDetailsService userDetailsService;

    @Mock
    ServerSecurityContextRepository securityContextRepository;

    @Mock
    LoginHandlerEnhancer loginHandlerEnhancer;

    MockServerWebExchange exchange;

    OAuth2AuthenticationToken original;

    OAuth2AuthenticationSession authenticationSession;

    @BeforeEach
    void setUp() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login/oauth2/code/github"));
        var principal = new DefaultOAuth2User(List.of(), Map.of("id", "oauth-user"), "id");
        original = new OAuth2AuthenticationToken(principal, List.of(), "github");
        authenticationSession =
                new OAuth2AuthenticationSession(userDetailsService, securityContextRepository, loginHandlerEnhancer);
    }

    @Test
    void shouldSaveHaloAuthenticationBeforeEnhancingLoginSuccess() {
        var userDetails = User.withUsername("halo-user")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        when(userDetailsService.findByUsername("halo-user")).thenReturn(Mono.just(userDetails));
        when(securityContextRepository.save(eq(exchange), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(eq(exchange), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Mono.empty());

        authenticationSession
                .establish(exchange, "halo-user", original)
                .as(StepVerifier::create)
                .verifyComplete();

        var contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).save(eq(exchange), contextCaptor.capture());
        var authentication = contextCaptor.getValue().getAuthentication();
        assertThat(authentication).isInstanceOf(HaloOAuth2AuthenticationToken.class);
        assertThat(authentication.getName()).isEqualTo("halo-user");
        assertThat(authentication.getPrincipal()).isSameAs(original.getPrincipal());
        assertThat(((HaloOAuth2AuthenticationToken) authentication).getOriginal())
                .isSameAs(original);

        var order = inOrder(securityContextRepository, loginHandlerEnhancer);
        order.verify(securityContextRepository).save(exchange, contextCaptor.getValue());
        order.verify(loginHandlerEnhancer).onLoginSuccess(exchange, authentication);
    }
}
