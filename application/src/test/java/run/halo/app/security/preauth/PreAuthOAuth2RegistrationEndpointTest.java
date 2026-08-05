package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.AgreementNotAcceptedException;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationSession;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationService;

@ExtendWith(MockitoExtension.class)
class PreAuthOAuth2RegistrationEndpointTest {

    @Mock
    OAuth2AuthenticationTokenCache tokenCache;

    @Mock
    AuthProviderService authProviderService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    AgreementPageFetcher agreementPageFetcher;

    @Mock
    OAuth2RegistrationService registrationService;

    @Mock
    OAuth2AuthenticationSession authenticationSession;

    @Mock
    ServerRequestCache requestCache;

    OAuth2AuthenticationToken token;
    AuthProvider provider;
    SystemSetting.User setting;
    AtomicReference<String> renderedView;
    AtomicReference<Map<String, Object>> renderedModel;
    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        var principal = new DefaultOAuth2User(List.of(), Map.of("id", "oauth-user"), "id");
        token = new OAuth2AuthenticationToken(principal, List.of(), "github");
        provider = provider("github", "GitHub");
        setting = new SystemSetting.User();
        setting.setAllowRegistration(true);
        renderedView = new AtomicReference<>();
        renderedModel = new AtomicReference<>();

        var endpoint = new PreAuthOAuth2RegistrationEndpoint(
                tokenCache,
                authProviderService,
                systemConfigFetcher,
                agreementPageFetcher,
                registrationService,
                authenticationSession,
                requestCache);
        ViewResolver viewResolver = (viewName, locale) -> {
            renderedView.set(viewName);
            return Mono.just((model, contentType, exchange) -> {
                renderedModel.set(new HashMap<>(model));
                return Mono.empty();
            });
        };
        webClient = WebTestClient.bindToRouterFunction(endpoint.preAuthOAuth2RegistrationEndpoints())
                .handlerStrategies(
                        HandlerStrategies.builder().viewResolver(viewResolver).build())
                .build();
    }

    @Test
    void shouldRedirectToLoginWhenCachedTokenIsMissingForGet() {
        when(tokenCache.getToken(any())).thenReturn(Mono.empty());

        webClient
                .get()
                .uri("/login?oauth2_select")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/login");
    }

    @Test
    void shouldRedirectToLoginWhenCachedTokenIsMissingForPost() {
        when(tokenCache.getToken(any())).thenReturn(Mono.empty());

        post("agreedToTerms=true").expectStatus().isFound().expectHeader().location("/login");
    }

    @Test
    void shouldRenderMatchingEnabledProviderRegistrationSettingAndAgreementPages() {
        var terms = Map.of("title", "Terms", "permalink", "/terms");
        when(tokenCache.getToken(any())).thenReturn(Mono.just(token));
        when(authProviderService.getEnabledProviders()).thenReturn(Flux.just(provider("gitlab", "GitLab"), provider));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(agreementPageFetcher.fetchAgreementPages()).thenReturn(Mono.just(List.of(terms)));

        webClient.get().uri("/login?oauth2_select").exchange().expectStatus().isOk();

        assertThat(renderedView.get()).isEqualTo("login_oauth2_select");
        assertThat(renderedModel.get())
                .containsEntry("provider", provider)
                .containsEntry("allowRegistration", true)
                .containsEntry("agreementPages", List.of(terms));
    }

    @Test
    void shouldRenderRegistrationClosedWhenServiceRejectsForgedPost() {
        setting.setAllowRegistration(false);
        givenSelectionModel(List.of());
        when(registrationService.register(token, true))
                .thenReturn(Mono.error(
                        new ServerWebInputException("The registration is not allowed by the administrator.")));

        post("agreedToTerms=true").expectStatus().isOk();

        assertThat(renderedModel.get())
                .containsEntry("allowRegistration", false)
                .containsEntry("error", "registration-closed");
        verify(tokenCache, never()).removeToken(any());
    }

    @Test
    void shouldRenderAgreementErrorWhenRequiredAgreementIsUnchecked() {
        givenSelectionModel(List.of(Map.of("title", "Terms", "permalink", "/terms")));
        when(registrationService.register(token, false))
                .thenReturn(Mono.error(new AgreementNotAcceptedException(
                        "Agreement not accepted.", "problemDetail.user.signup.agreement-not-accepted", null)));

        post("agreedToTerms=false").expectStatus().isOk();

        assertThat(renderedModel.get()).containsEntry("error", "agreement-not-accepted");
        verify(tokenCache, never()).removeToken(any());
    }

    @Test
    void shouldFailClosedBeforeRegistrationWhenConfiguredAgreementPageCannotBeLoaded() {
        when(tokenCache.getToken(any())).thenReturn(Mono.just(token));
        when(authProviderService.getEnabledProviders()).thenReturn(Flux.just(provider));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(agreementPageFetcher.fetchAgreementPages())
                .thenReturn(Mono.error(new IllegalStateException("Required agreement page not found: terms")));

        post("agreedToTerms=true").expectStatus().is5xxServerError();

        verify(registrationService, never()).register(any(), eq(true));
    }

    @Test
    void shouldEstablishSessionAndRedirectToProfileCompletion() {
        givenSelectionModel(List.of());
        when(registrationService.register(token, false))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", true)));
        when(authenticationSession.establish(any(), eq("alice"), eq(token))).thenReturn(Mono.empty());

        post("agreedToTerms=false").expectStatus().isFound().expectHeader().location("/complete-profile");

        verify(authenticationSession).establish(any(), eq("alice"), eq(token));
        verify(tokenCache, never()).removeToken(any());
    }

    @Test
    void shouldRedirectCompleteRegistrationToSavedUri() {
        givenSelectionModel(List.of());
        when(registrationService.register(token, false))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", false)));
        when(authenticationSession.establish(any(), eq("alice"), eq(token))).thenReturn(Mono.empty());
        when(requestCache.getRedirectUri(any())).thenReturn(Mono.just(URI.create("/dashboard")));

        post("agreedToTerms=false").expectStatus().isFound().expectHeader().location("/dashboard");
    }

    @Test
    void shouldDefaultCompleteRegistrationRedirectToUserCenter() {
        givenSelectionModel(List.of());
        when(registrationService.register(token, false))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", false)));
        when(authenticationSession.establish(any(), eq("alice"), eq(token))).thenReturn(Mono.empty());
        when(requestCache.getRedirectUri(any())).thenReturn(Mono.empty());

        post("agreedToTerms=false").expectStatus().isFound().expectHeader().location("/uc");
    }

    @Test
    void shouldPropagateSessionEstablishmentFailureWithoutRenderingSelectionPage() {
        givenSelectionModel(List.of());
        when(registrationService.register(token, false))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", true)));
        when(authenticationSession.establish(any(), eq("alice"), eq(token)))
                .thenReturn(Mono.error(new IllegalStateException("session establishment failed")));

        post("agreedToTerms=false").expectStatus().is5xxServerError();

        assertThat(renderedView.get()).isNull();
    }

    @Test
    void shouldPropagateRequestCacheFailureWithoutRenderingSelectionPage() {
        givenSelectionModel(List.of());
        when(registrationService.register(token, false))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", false)));
        when(authenticationSession.establish(any(), eq("alice"), eq(token))).thenReturn(Mono.empty());
        when(requestCache.getRedirectUri(any()))
                .thenReturn(Mono.error(new IllegalStateException("request cache failed")));

        post("agreedToTerms=false").expectStatus().is5xxServerError();

        assertThat(renderedView.get()).isNull();
    }

    @Test
    void shouldRenderDefaultRoleMissingError() {
        givenSelectionModel(List.of());
        when(registrationService.register(token, false))
                .thenReturn(Mono.error(
                        new ServerWebInputException("The default role is not configured by the administrator.")));

        post("agreedToTerms=false").expectStatus().isOk();

        assertThat(renderedModel.get()).containsEntry("error", "default-role-missing");
        verify(tokenCache, never()).removeToken(any());
    }

    @Test
    void shouldRenderRegistrationFailureWithoutRemovingCachedToken() {
        givenSelectionModel(List.of());
        when(registrationService.register(token, false))
                .thenReturn(Mono.error(new IllegalStateException("registration failed")));

        post("agreedToTerms=false").expectStatus().isOk();

        assertThat(renderedView.get()).isEqualTo("login_oauth2_select");
        assertThat(renderedModel.get())
                .containsEntry("provider", provider)
                .containsEntry("allowRegistration", true)
                .containsEntry("agreementPages", List.of())
                .containsEntry("error", "registration-failed");
        verify(tokenCache, never()).removeToken(any());
        verify(authenticationSession, never()).establish(any(), any(), any());
    }

    private WebTestClient.ResponseSpec post(String body) {
        return webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .exchange();
    }

    private void givenSelectionModel(List<Map<String, String>> agreementPages) {
        when(tokenCache.getToken(any())).thenReturn(Mono.just(token));
        when(authProviderService.getEnabledProviders()).thenReturn(Flux.just(provider));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(agreementPageFetcher.fetchAgreementPages()).thenReturn(Mono.just(agreementPages));
    }

    private static AuthProvider provider(String name, String displayName) {
        var provider = new AuthProvider();
        var metadata = new Metadata();
        metadata.setName(name);
        provider.setMetadata(metadata);
        var spec = new AuthProvider.AuthProviderSpec();
        spec.setDisplayName(displayName);
        provider.setSpec(spec);
        return provider;
    }
}
