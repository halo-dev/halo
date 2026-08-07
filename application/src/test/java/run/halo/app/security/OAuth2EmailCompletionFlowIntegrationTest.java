package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationSession;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(OAuth2EmailCompletionFlowIntegrationTest.FlowRouteConfiguration.class)
class OAuth2EmailCompletionFlowIntegrationTest {

    private static final String USERNAME = "oauth-user";
    private static final String EMAIL = "oauth-user@example.com";

    WebTestClient webClient;

    @Autowired
    ApplicationContext applicationContext;

    @MockitoBean
    UserService userService;

    @MockitoSpyBean
    SystemConfigFetcher systemConfigFetcher;

    @MockitoBean
    DefaultUserDetailService userDetailsService;

    @MockitoBean
    LoginHandlerEnhancer loginHandlerEnhancer;

    @MockitoBean
    EmailVerificationService emailVerificationService;

    @MockitoBean
    InitializationStateGetter initializationStateGetter;

    @MockitoBean
    RateLimiterRegistry rateLimiterRegistry;

    User user;
    SystemSetting.User setting;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
        user = user(false);
        setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(true);

        when(userService.getUser(anyString())).thenAnswer(invocation -> Mono.just(user));
        when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
        doReturn(Mono.just(setting))
                .when(systemConfigFetcher)
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());
        when(initializationStateGetter.userInitialized()).thenReturn(Mono.just(true));
        when(initializationStateGetter.dataInitialized()).thenReturn(Mono.just(true));
        when(rateLimiterRegistry.rateLimiter(anyString(), eq("verify-email")))
                .thenAnswer(invocation -> RateLimiter.ofDefaults(invocation.getArgument(0)));
        givenAuthorities("ROLE_authenticated");
    }

    @Test
    void shouldRedirectUnverifiedOAuth2SessionToReachableCompletionPage() {
        var session = establishOAuth2Session();

        webClient
                .get()
                .uri("/uc")
                .cookie(session.getName(), session.getValue())
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/complete-profile");

        webClient
                .get()
                .uri("/complete-profile")
                .cookie(session.getName(), session.getValue())
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldRenderLogoutOptionOnCompletionPage() {
        var session = establishOAuth2Session();

        webClient
                .get()
                .uri("/complete-profile")
                .cookie(session.getName(), session.getValue())
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("href=\"/logout\"").contains(">Log out</a>"));
    }

    @Test
    void shouldRejectUnverifiedUserApiRequestWithEmailNotSetProblem() {
        webClient
                .mutateWith(mockUser(USERNAME).roles("authenticated"))
                .get()
                .uri("/apis/api.console.halo.run/v1alpha1/users/-")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isForbidden()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("email-not-set");
    }

    @Test
    void shouldRejectUnverifiedUserApiRequestAcceptingHtmlWithoutRedirect() {
        webClient
                .mutateWith(mockUser(USERNAME).roles("authenticated"))
                .get()
                .uri("/apis/api.console.halo.run/v1alpha1/users/-")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isForbidden()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("email-not-set");
    }

    @Test
    void shouldNotInterceptRawOAuth2CallbackOrResolveExternalSubjectAsHaloUser() {
        var principal = new DefaultOAuth2User(List.of(), Map.of("id", USERNAME), "id");
        var rawOAuth2 = new OAuth2AuthenticationToken(principal, List.of(), "test-provider");

        webClient
                .mutateWith(mockAuthentication(rawOAuth2))
                .get()
                .uri("/login/oauth2/code/test-provider")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/login?oauth2_select");

        verify(userService, never()).getUser(USERNAME);
    }

    @Test
    void shouldReleaseVerifiedOAuth2UserToOriginalRoute() {
        user.getSpec().setEmailVerified(true);
        var session = establishOAuth2Session();

        expectUserCenter(session);
    }

    @Test
    void shouldReleaseSuperRoleUserWithoutVerifiedEmail() {
        givenAuthorities("ROLE_super-role");
        var session = establishOAuth2Session();

        expectUserCenter(session);
    }

    @Test
    void shouldReleaseUnverifiedUserWhenVerificationSettingIsDisabled() {
        setting.setMustVerifyEmailOnRegistration(false);
        var session = establishOAuth2Session();

        expectUserCenter(session);
    }

    @Test
    void shouldReleaseSavedUserCenterRequestAfterCompletingVerification() {
        MetadataUtil.nullSafeAnnotations(user).put(User.EMAIL_TO_VERIFY, EMAIL);
        when(emailVerificationService.verify(USERNAME, "123456")).thenAnswer(invocation -> {
            user.getSpec().setEmail(EMAIL);
            user.getSpec().setEmailVerified(true);
            return Mono.empty();
        });
        var session = establishOAuth2Session();

        webClient
                .get()
                .uri("/uc?source=saved")
                .cookie(session.getName(), session.getValue())
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/complete-profile");

        webClient
                .mutateWith(csrf())
                .post()
                .uri("/complete-profile")
                .cookie(session.getName(), session.getValue())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("email=oauth-user%40example.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc?source=saved");

        assertThat(user.getSpec().isEmailVerified()).isTrue();
        expectUserCenter(session);
    }

    private HttpCookie establishOAuth2Session() {
        var result = webClient
                .mutateWith(csrf())
                .post()
                .uri("/login/test/oauth2-session")
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectCookie()
                .exists("SESSION")
                .expectBody()
                .returnResult();
        return result.getResponseCookies().getFirst("SESSION");
    }

    private void expectUserCenter(HttpCookie session) {
        webClient
                .get()
                .uri("/uc")
                .cookie(session.getName(), session.getValue())
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("user center");
    }

    private void givenAuthorities(String... authorities) {
        UserDetails details = org.springframework.security.core.userdetails.User.withUsername(USERNAME)
                .password("password")
                .authorities(authorities)
                .build();
        when(userDetailsService.findByUsername(USERNAME)).thenReturn(Mono.just(details));
    }

    private static User user(boolean emailVerified) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(USERNAME);
        user.getSpec().setEmail(EMAIL);
        user.getSpec().setEmailVerified(emailVerified);
        return user;
    }

    private static OAuth2AuthenticationToken oauth2Token() {
        var principal = new DefaultOAuth2User(List.of(), Map.of("id", "oauth-identity"), "id");
        return new OAuth2AuthenticationToken(principal, List.of(), "test-provider");
    }

    @TestConfiguration
    static class FlowRouteConfiguration {

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        RouterFunction<ServerResponse> flowRoutes(OAuth2AuthenticationSession authenticationSession) {
            return RouterFunctions.route()
                    .POST(
                            "/login/test/oauth2-session",
                            request -> authenticationSession
                                    .establish(request.exchange(), USERNAME, oauth2Token())
                                    .then(ServerResponse.noContent().build()))
                    .GET("/login/oauth2/code/test-provider", request -> Mono.empty())
                    .GET(
                            "/uc",
                            request -> ServerResponse.ok()
                                    .contentType(MediaType.TEXT_HTML)
                                    .bodyValue("user center"))
                    .build();
        }
    }
}
