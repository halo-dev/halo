package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationService;

@ExtendWith(MockitoExtension.class)
class PreAuthOAuth2RegistrationEndpointTest {

    @Mock
    OAuth2RegistrationService registrationService;

    @Mock
    OAuth2AuthenticationTokenCache tokenCache;

    @Mock
    ServerSecurityContextRepository securityContextRepository;

    @Mock
    ReactiveUserDetailsService userDetailsService;

    @Mock
    LoginHandlerEnhancer loginHandlerEnhancer;

    @Mock
    ServerRequestCache requestCache;

    @Mock
    GlobalInfoService globalInfoService;

    @Mock
    AuthProviderService authProviderService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    ReactiveExtensionClient extensionClient;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        var endpoint = new PreAuthOAuth2RegistrationEndpoint(
                registrationService,
                tokenCache,
                securityContextRepository,
                userDetailsService,
                loginHandlerEnhancer,
                requestCache,
                globalInfoService,
                authProviderService,
                systemConfigFetcher,
                extensionClient);
        webClient = WebTestClient.bindToRouterFunction(endpoint.preAuthOAuth2RegistrationEndpoints())
                .build();
    }

    OAuth2AuthenticationToken token() {
        var user = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_authenticated")),
                Map.of("sub", "alice", "email", "alice@example.com"),
                "sub");
        return new OAuth2AuthenticationToken(user, List.of(), "github");
    }

    @Test
    void shouldRegisterAndRedirectToCompleteProfileWhenEmailIncomplete() {
        when(tokenCache.getToken(any())).thenReturn(Mono.just(token()));
        when(registrationService.register(any(), eq(false)))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", true)));
        var userDetails =
                User.withUsername("alice").password("").roles("authenticated").build();
        when(userDetailsService.findByUsername("alice")).thenReturn(Mono.just(userDetails));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("agreedToTerms=false")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/complete-profile");
    }

    @Test
    void shouldRedirectToSavedRequestWhenEmailIsComplete() {
        when(tokenCache.getToken(any())).thenReturn(Mono.just(token()));
        when(registrationService.register(any(), eq(false)))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", false)));
        var userDetails =
                User.withUsername("alice").password("").roles("authenticated").build();
        when(userDetailsService.findByUsername("alice")).thenReturn(Mono.just(userDetails));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());
        when(requestCache.getRedirectUri(any())).thenReturn(Mono.just(URI.create("/uc")));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("agreedToTerms=false")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");
    }

    @Test
    void shouldRedirectToLoginWhenTokenMissing() {
        when(tokenCache.getToken(any())).thenReturn(Mono.empty());

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("agreedToTerms=false")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/login");
    }
}
