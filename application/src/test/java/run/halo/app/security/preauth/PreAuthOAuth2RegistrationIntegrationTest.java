package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserLoginOrLogoutProcessing;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.DefaultUserDetailService;
import run.halo.app.security.LoginParameterRequestCache;
import run.halo.app.security.authentication.oauth2.DefaultOAuth2LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationService;
import run.halo.app.security.authentication.rememberme.RememberMeServices;
import run.halo.app.security.device.DeviceService;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(PreAuthOAuth2RegistrationIntegrationTest.TokenCacheRouteConfiguration.class)
class PreAuthOAuth2RegistrationIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @MockitoBean
    OAuth2RegistrationService registrationService;

    @MockitoBean
    AuthProviderService authProviderService;

    @MockitoSpyBean
    SystemConfigFetcher systemConfigFetcher;

    @MockitoBean
    AgreementPageFetcher agreementPageFetcher;

    @MockitoBean
    DefaultUserDetailService userDetailsService;

    @MockitoBean
    UserConnectionService connectionService;

    @MockitoBean
    RememberMeServices rememberMeServices;

    @MockitoBean
    DeviceService deviceService;

    @MockitoBean
    UserLoginOrLogoutProcessing userLoginOrLogoutProcessing;

    @MockitoBean
    LoginParameterRequestCache parameterRequestCache;

    @MockitoSpyBean
    OAuth2AuthenticationTokenCache tokenCache;

    @MockitoSpyBean
    ServerSecurityContextRepository securityContextRepository;

    @MockitoSpyBean
    DefaultOAuth2LoginHandlerEnhancer oauth2LoginHandlerEnhancer;

    @Test
    void shouldRenderSelectionTemplateWithProviderAndBindLink() {
        givenRegistrationDependencies();
        var sessionCookie = cacheOAuth2Token();

        webClient
                .get()
                .uri("/login?oauth2_select")
                .cookie(sessionCookie.getName(), sessionCookie.getValue())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("GitHub").contains("href=\"/login?oauth2_bind\""));
    }

    @Test
    void shouldStoreHaloAuthenticationAndLetEnhancerRemoveCachedToken() {
        givenRegistrationDependencies();
        var sessionCookie = cacheOAuth2Token();
        clearInvocations(tokenCache, securityContextRepository, oauth2LoginHandlerEnhancer);

        webClient
                .mutateWith(csrf())
                .post()
                .uri("/login/oauth2/register")
                .cookie(sessionCookie.getName(), sessionCookie.getValue())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("agreedToTerms=false")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");

        var contextCaptor = org.mockito.ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).save(any(), contextCaptor.capture());
        var authentication = contextCaptor.getValue().getAuthentication();
        assertThat(authentication).isInstanceOf(HaloOAuth2AuthenticationToken.class);
        assertThat(authentication.getName()).isEqualTo("alice");
        assertThat(((HaloOAuth2AuthenticationToken) authentication)
                        .getOriginal()
                        .getAuthorizedClientRegistrationId())
                .isEqualTo("github");

        verify(oauth2LoginHandlerEnhancer).loginSuccess(any(), eq(authentication));
        verify(tokenCache).removeToken(any());
        verify(tokenCache, never()).saveToken(any(), any());
    }

    private HttpCookie cacheOAuth2Token() {
        var result = webClient
                .get()
                .uri("/login/oauth2/test-cache-token")
                .exchange()
                .expectStatus()
                .isOk()
                .expectCookie()
                .exists("SESSION")
                .expectBody()
                .returnResult();
        return result.getResponseCookies().getFirst("SESSION");
    }

    private void givenRegistrationDependencies() {
        var provider = new AuthProvider();
        var metadata = new Metadata();
        metadata.setName("github");
        provider.setMetadata(metadata);
        var spec = new AuthProvider.AuthProviderSpec();
        spec.setDisplayName("GitHub");
        provider.setSpec(spec);
        var setting = new SystemSetting.User();
        setting.setAllowRegistration(true);

        when(authProviderService.getEnabledProviders()).thenReturn(Flux.just(provider));
        doReturn(Mono.just(setting))
                .when(systemConfigFetcher)
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
        when(agreementPageFetcher.fetchAgreementPages()).thenReturn(Mono.just(List.of()));
        when(registrationService.register(any(), eq(false)))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", false)));
        when(userDetailsService.findByUsername("alice"))
                .thenReturn(Mono.just(User.withUsername("alice")
                        .password("password")
                        .roles("user")
                        .build()));
        when(rememberMeServices.loginSuccess(any(), any())).thenReturn(Mono.empty());
        when(rememberMeServices.autoLogin(any())).thenReturn(Mono.empty());
        when(deviceService.loginSuccess(any(), any())).thenReturn(Mono.empty());
        when(deviceService.changeSessionId(any())).thenReturn(Mono.empty());
        when(connectionService.updateUserConnectionIfPresent(eq("github"), any()))
                .thenReturn(Mono.empty());
        when(connectionService.createUserConnection(eq("alice"), eq("github"), any()))
                .thenReturn(Mono.just(new UserConnection()));
        when(userLoginOrLogoutProcessing.loginProcessing("alice")).thenReturn(Mono.empty());
        when(parameterRequestCache.removeParameter(any(), any())).thenReturn(Mono.empty());
    }

    private static OAuth2AuthenticationToken oauth2Token() {
        var principal = new DefaultOAuth2User(List.of(), Map.of("id", "oauth-user"), "id");
        return new OAuth2AuthenticationToken(principal, List.of(), "github");
    }

    @TestConfiguration
    static class TokenCacheRouteConfiguration {

        @Bean
        @Order(-1)
        RouterFunction<ServerResponse> tokenCacheRoute(OAuth2AuthenticationTokenCache tokenCache) {
            return RouterFunctions.route()
                    .GET(
                            "/login/oauth2/test-cache-token",
                            request -> tokenCache
                                    .saveToken(request.exchange(), oauth2Token())
                                    .then(ServerResponse.ok().build()))
                    .build();
        }
    }
}
