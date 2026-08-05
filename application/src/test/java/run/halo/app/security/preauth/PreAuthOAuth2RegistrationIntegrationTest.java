package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.authentication.oauth2.DefaultOAuth2LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationService;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PreAuthOAuth2RegistrationIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    DefaultOAuth2LoginHandlerEnhancer loginHandlerEnhancer;

    @MockitoBean
    OAuth2AuthenticationTokenCache tokenCache;

    @MockitoBean
    OAuth2RegistrationService registrationService;

    @MockitoBean
    UserConnectionService connectionService;

    @BeforeEach
    void setUp() {
        loginHandlerEnhancer.setOauth2TokenCache(tokenCache);
        webClient = webClient.mutateWith(SecurityMockServerConfigurers.csrf());
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("alice");
        var spec = new User.UserSpec();
        spec.setDisplayName("Alice");
        spec.setPassword("{noop}test-password");
        spec.setEmail("alice@example.com");
        spec.setEmailVerified(true);
        spec.setRegisteredAt(Instant.now());
        user.setSpec(spec);
        client.create(user).block();
    }

    @Test
    void shouldCleanupCachedTokenAfterRegistration() {
        var oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_authenticated")),
                Map.of("sub", "alice", "email", "alice@example.com"),
                "sub");
        var token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");
        when(tokenCache.getToken(any())).thenReturn(Mono.just(token));
        when(tokenCache.removeToken(any())).thenReturn(Mono.empty());
        when(registrationService.register(any(), eq(false)))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", false)));
        when(connectionService.updateUserConnectionIfPresent(any(), any())).thenReturn(Mono.empty());
        when(connectionService.createUserConnection(any(), any(), any())).thenReturn(Mono.just(new UserConnection()));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .header("User-Agent", "test-agent")
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("agreedToTerms=false")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");

        verify(tokenCache).removeToken(any());
        verify(tokenCache, never()).saveToken(any(), any());
    }
}
