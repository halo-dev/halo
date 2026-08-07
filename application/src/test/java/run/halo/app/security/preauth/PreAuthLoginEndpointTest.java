package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.LoginParameterRequestCache;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2BindIntent;

@ExtendWith(MockitoExtension.class)
class PreAuthLoginEndpointTest {

    private static final String OAUTH2_IDENTITY = "4d2f4d279e39d96d972a1afa6bb5920871c7b4b9f068b1bfca47898ec655c41f";

    @Mock
    CryptoService cryptoService;

    @Mock
    GlobalInfoService globalInfoService;

    @Mock
    AuthProviderService authProviderService;

    @Mock
    LoginParameterRequestCache parameterRequestCache;

    @Mock
    OAuth2AuthenticationTokenCache tokenCache;

    @Mock
    OAuth2BindIntent bindIntent;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        lenient().when(cryptoService.readPublicKey()).thenReturn(Mono.empty());
        lenient().when(globalInfoService.getGlobalInfo()).thenReturn(Mono.empty());
        lenient().when(authProviderService.getEnabledProviders()).thenReturn(Flux.empty());
        lenient().when(parameterRequestCache.getParameter(any(), any())).thenReturn(Mono.empty());
        var endpoint = new PreAuthLoginEndpoint(
                cryptoService, globalInfoService, authProviderService, parameterRequestCache, tokenCache, bindIntent);
        var viewResolver = (org.springframework.web.reactive.result.view.ViewResolver)
                (viewName, locale) -> Mono.just((model, contentType, exchange) -> Mono.empty());
        webClient = WebTestClient.bindToRouterFunction(endpoint.preAuthLoginEndpoints())
                .handlerStrategies(
                        HandlerStrategies.builder().viewResolver(viewResolver).build())
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login", "/login?signup", "/login?error=invalid-credential"})
    void shouldClearCachedOAuth2TokenForOrdinaryLogin(String uri) {
        when(tokenCache.removeToken(any())).thenReturn(Mono.empty());
        when(bindIntent.clear(any())).thenReturn(Mono.empty());

        webClient.get().uri(uri).exchange().expectStatus().isOk();

        verify(tokenCache).removeToken(any());
        verify(bindIntent).clear(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login?oauth2_select"})
    void shouldPreserveCachedOAuth2TokenWhileSelectingOAuth2Registration(String uri) {
        when(bindIntent.clear(any())).thenReturn(Mono.empty());

        webClient.get().uri(uri).exchange().expectStatus().isOk();
        verify(tokenCache, never()).removeToken(any());
        verify(bindIntent).clear(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login?oauth2_bind&oauth2Identity=" + OAUTH2_IDENTITY})
    void shouldRecordExplicitBindIntentWhilePreservingCachedToken(String uri) {
        when(tokenCache.getToken(any())).thenReturn(Mono.just(oauth2Token()));
        when(bindIntent.save(any(), eq(OAUTH2_IDENTITY))).thenReturn(Mono.empty());

        webClient.get().uri(uri).exchange().expectStatus().isOk();

        verify(tokenCache, never()).removeToken(any());
        verify(bindIntent).save(any(), eq(OAUTH2_IDENTITY));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login?oauth2_bind&oauth2Identity=stale"})
    void shouldRejectBindEntryWhenOAuth2IdentityChanged(String uri) {
        when(tokenCache.getToken(any())).thenReturn(Mono.just(oauth2Token()));
        when(tokenCache.removeToken(any())).thenReturn(Mono.empty());
        when(bindIntent.clear(any())).thenReturn(Mono.empty());

        webClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/login?error=oauth2-flow-expired");

        verify(bindIntent, never()).save(any(), any());
        verify(tokenCache).removeToken(any());
        verify(bindIntent).clear(any());
    }

    private static OAuth2AuthenticationToken oauth2Token() {
        var principal = new DefaultOAuth2User(List.of(), Map.of("id", "external-user"), "id");
        return new OAuth2AuthenticationToken(principal, List.of(), "github");
    }
}
