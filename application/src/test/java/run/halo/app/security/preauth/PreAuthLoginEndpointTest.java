package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.LoginParameterRequestCache;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;

@ExtendWith(MockitoExtension.class)
class PreAuthLoginEndpointTest {

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

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        when(cryptoService.readPublicKey()).thenReturn(Mono.empty());
        when(globalInfoService.getGlobalInfo()).thenReturn(Mono.empty());
        when(authProviderService.getEnabledProviders()).thenReturn(Flux.empty());
        when(parameterRequestCache.getParameter(any(), any())).thenReturn(Mono.empty());
        var endpoint = new PreAuthLoginEndpoint(
                cryptoService, globalInfoService, authProviderService, parameterRequestCache, tokenCache);
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

        webClient.get().uri(uri).exchange().expectStatus().isOk();

        verify(tokenCache).removeToken(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login?oauth2_bind", "/login?oauth2_select"})
    void shouldPreserveCachedOAuth2TokenWhileContinuingOAuth2Flow(String uri) {
        webClient.get().uri(uri).exchange().expectStatus().isOk();

        verify(tokenCache, never()).removeToken(any());
    }
}
