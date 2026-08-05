package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.security.authentication.SecurityConfigurer;

@ExtendWith(MockitoExtension.class)
class CompleteProfileEndpointTest {

    private static final String USERNAME = "alice";
    private static final String TEST_USERNAME_HEADER = "X-Test-Username";

    @Mock
    UserService userService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    EmailVerificationService emailVerificationService;

    @Mock
    RateLimiterRegistry rateLimiterRegistry;

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ServerRequestCache requestCache;

    User user;
    SystemSetting.User setting;
    ValidatorFactory validatorFactory;
    AtomicReference<String> renderedView;
    AtomicReference<Map<String, Object>> renderedModel;
    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        user = user("Initial@Example.com", false);
        setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(true);
        validatorFactory = Validation.buildDefaultValidatorFactory();
        renderedView = new AtomicReference<>();
        renderedModel = new AtomicReference<>();

        lenient().when(userService.getUser(USERNAME)).thenReturn(Mono.just(user));
        lenient()
                .when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        lenient().when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
        lenient()
                .when(emailVerificationService.sendVerificationCode(anyString(), anyString()))
                .thenReturn(Mono.empty());
        lenient()
                .when(emailVerificationService.verify(anyString(), anyString()))
                .thenReturn(Mono.empty());
        lenient().when(client.update(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        lenient().when(requestCache.getRedirectUri(any())).thenReturn(Mono.empty());

        var endpoint = new CompleteProfileEndpoint(
                userService,
                systemConfigFetcher,
                emailVerificationService,
                rateLimiterRegistry,
                client,
                requestCache,
                new SpringValidatorAdapter(validatorFactory.getValidator()));
        ViewResolver viewResolver = (viewName, locale) -> {
            renderedView.set(viewName);
            return Mono.just((model, contentType, exchange) -> {
                renderedModel.set(new HashMap<>(model));
                return Mono.empty();
            });
        };
        webClient = WebTestClient.bindToRouterFunction(endpoint.completeProfileEndpoints())
                .handlerStrategies(
                        HandlerStrategies.builder().viewResolver(viewResolver).build())
                .webFilter(testAuthenticationFilter())
                .webFilter(new WebFilterChainProxy(authorizationFilterChain()))
                .build();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @MethodSource("completeProfileRequests")
    void shouldRejectAnonymousRequests(HttpMethod method, String path, MediaType contentType, String body) {
        var request = webClient.method(method).uri(path);
        var response = body == null
                ? request.exchange()
                : request.contentType(contentType).bodyValue(body).exchange();

        response.expectStatus().isUnauthorized();

        verifyNoInteractions(userService, emailVerificationService, client);
    }

    @Test
    void shouldPrefillCurrentEmailAndExposeRequiredSetting() {
        authenticatedGet().exchange().expectStatus().isOk();

        assertThat(renderedView.get()).isEqualTo("complete_profile");
        assertThat(renderedModel.get()).containsEntry("mustVerifyEmailOnRegistration", true);
        assertThat(renderedModel.get().get("form"))
                .isEqualTo(new CompleteProfileEndpoint.CompleteProfileForm("Initial@Example.com", null));
    }

    @Test
    void shouldRedirectVerifiedUserToSavedTarget() {
        user.getSpec().setEmailVerified(true);
        when(requestCache.getRedirectUri(any())).thenReturn(Mono.just(URI.create("/dashboard")));

        authenticatedGet().exchange().expectStatus().isFound().expectHeader().location("/dashboard");

        assertVerifiedUserDidNotAct();
    }

    @Test
    void shouldRedirectVerifiedUserToUserCenterByDefault() {
        user.getSpec().setEmailVerified(true);

        authenticatedGet().exchange().expectStatus().isFound().expectHeader().location("/uc");

        assertVerifiedUserDidNotAct();
    }

    @Test
    void shouldNotSendCodeForVerifiedUser() {
        user.getSpec().setEmailVerified(true);
        when(requestCache.getRedirectUri(any())).thenReturn(Mono.just(URI.create("/dashboard")));

        sendCode("Verified@Example.com")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/dashboard");

        assertVerifiedUserDidNotAct();
    }

    @Test
    void shouldNotMutateEmailForVerifiedUser() {
        user.getSpec().setEmailVerified(true);

        submit("email=changed%40example.com")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");

        assertThat(user.getSpec().getEmail()).isEqualTo("Initial@Example.com");
        assertThat(user.getSpec().isEmailVerified()).isTrue();
        assertVerifiedUserDidNotAct();
    }

    @Test
    void shouldRejectInvalidSendCodeEmailBeforeSending() {
        authenticatedJsonPost("/complete-profile/send-email-code", "{\"email\":\"invalid\"}")
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(userService, never()).checkEmailAlreadyVerified(anyString());
        verify(emailVerificationService, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void shouldRejectOccupiedSendCodeEmailAfterNormalization() {
        when(userService.checkEmailAlreadyVerified("taken@example.com")).thenReturn(Mono.just(true));

        sendCode("Taken@Example.COM").exchange().expectStatus().isBadRequest();

        verify(userService).checkEmailAlreadyVerified("taken@example.com");
        verify(emailVerificationService, never()).sendVerificationCode(anyString(), anyString());
        verifyNoInteractions(rateLimiterRegistry);
    }

    @Test
    void shouldSendCodeWithNormalizedEmailAndPerUserRateLimiter() {
        allowRateLimiter("send-email-verification-code-" + USERNAME);

        sendCode("Alice@Example.COM").exchange().expectStatus().isAccepted();

        verify(userService).checkEmailAlreadyVerified("alice@example.com");
        verify(rateLimiterRegistry)
                .rateLimiter("send-email-verification-code-" + USERNAME, "send-email-verification-code");
        verify(emailVerificationService).sendVerificationCode(USERNAME, "alice@example.com");
    }

    @Test
    void shouldRateLimitSendingCodeBeforeCallingService() {
        exhaustRateLimiter("send-email-verification-code-" + USERNAME);

        sendCode("alice@example.com").exchange().expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        verify(emailVerificationService, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void shouldRejectInvalidFormEmail() {
        submit("email=invalid&emailCode=").exchange().expectStatus().isOk();

        assertFieldError("email", "form.error.emailInvalid");
        verify(userService, never()).checkEmailAlreadyVerified(anyString());
        verify(emailVerificationService, never()).verify(anyString(), anyString());
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldRejectOccupiedEmailBeforeRequiredVerification() {
        when(userService.checkEmailAlreadyVerified("taken@example.com")).thenReturn(Mono.just(true));

        submit("email=Taken%40Example.COM&emailCode=123456")
                .exchange()
                .expectStatus()
                .isOk();

        assertFieldError("email", "form.error.emailTaken");
        verify(userService).checkEmailAlreadyVerified("taken@example.com");
        verify(emailVerificationService, never()).verify(anyString(), anyString());
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldRequireCodeWhenVerificationIsRequired() {
        submit("email=alice%40example.com&emailCode=").exchange().expectStatus().isOk();

        assertFieldError("emailCode", "form.error.codeRequired");
        verify(emailVerificationService, never()).verify(anyString(), anyString());
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldRejectCodeForStalePendingEmail() {
        MetadataUtil.nullSafeAnnotations(user).put(User.EMAIL_TO_VERIFY, "first@example.com");

        submit("email=second%40example.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .isOk();

        assertFieldError("email", "form.error.emailChanged");
        verify(userService).checkEmailAlreadyVerified("second@example.com");
        verify(emailVerificationService, never()).verify(anyString(), anyString());
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldRejectInvalidRequiredCodeWithoutUpdatingUser() {
        MetadataUtil.nullSafeAnnotations(user).put(User.EMAIL_TO_VERIFY, "alice@example.com");
        allowRateLimiter("verify-email-" + USERNAME);
        when(emailVerificationService.verify(USERNAME, "654321")).thenReturn(Mono.error(new EmailVerificationFailed()));

        submit("email=alice%40example.com&emailCode=654321")
                .exchange()
                .expectStatus()
                .isOk();

        assertFieldError("emailCode", "form.error.codeInvalid");
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldRateLimitRequiredVerification() {
        MetadataUtil.nullSafeAnnotations(user).put(User.EMAIL_TO_VERIFY, "alice@example.com");
        exhaustRateLimiter("verify-email-" + USERNAME);

        submit("email=alice%40example.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .isOk();

        assertFieldError("emailCode", "form.error.rateLimitExceeded");
        verify(rateLimiterRegistry).rateLimiter("verify-email-" + USERNAME, "verify-email");
        verify(emailVerificationService, never()).verify(anyString(), anyString());
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldVerifyRequiredEmailAndRedirectToSavedTarget() {
        MetadataUtil.nullSafeAnnotations(user).put(User.EMAIL_TO_VERIFY, "Alice@Example.COM");
        allowRateLimiter("verify-email-" + USERNAME);
        when(requestCache.getRedirectUri(any())).thenReturn(Mono.just(URI.create("/dashboard")));

        submit("email=alice%40example.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/dashboard");

        verify(userService).checkEmailAlreadyVerified("alice@example.com");
        verify(rateLimiterRegistry).rateLimiter("verify-email-" + USERNAME, "verify-email");
        verify(emailVerificationService).verify(USERNAME, "123456");
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldSaveNormalizedUnverifiedEmailWhenVerificationIsOptionalAndCodeIsBlank() {
        setting.setMustVerifyEmailOnRegistration(false);
        user.getSpec().setEmailVerified(false);

        submit("email=Optional%40Example.COM&emailCode=")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");

        assertThat(user.getSpec().getEmail()).isEqualTo("optional@example.com");
        assertThat(user.getSpec().isEmailVerified()).isFalse();
        verify(userService).checkEmailAlreadyVerified("optional@example.com");
        verify(client).update(user);
        verify(emailVerificationService, never()).verify(anyString(), anyString());
    }

    @Test
    void shouldKeepServiceVerifiedEmailWhenOptionalCodeIsValid() {
        setting.setMustVerifyEmailOnRegistration(false);
        MetadataUtil.nullSafeAnnotations(user).put(User.EMAIL_TO_VERIFY, "Optional@Example.COM");
        allowRateLimiter("verify-email-" + USERNAME);
        when(emailVerificationService.verify(USERNAME, "123456")).thenReturn(Mono.fromRunnable(() -> {
            user.getSpec().setEmail("optional@example.com");
            user.getSpec().setEmailVerified(true);
        }));

        submit("email=optional%40example.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");

        assertThat(user.getSpec().getEmail()).isEqualTo("optional@example.com");
        assertThat(user.getSpec().isEmailVerified()).isTrue();
        verify(emailVerificationService).verify(USERNAME, "123456");
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldRejectOptionalCodeForStalePendingEmail() {
        setting.setMustVerifyEmailOnRegistration(false);
        MetadataUtil.nullSafeAnnotations(user).put(User.EMAIL_TO_VERIFY, "first@example.com");

        submit("email=second%40example.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .isOk();

        assertFieldError("email", "form.error.emailChanged");
        verify(emailVerificationService, never()).verify(anyString(), anyString());
        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldNotDowngradeInvalidOptionalCodeToUnverifiedSave() {
        setting.setMustVerifyEmailOnRegistration(false);
        MetadataUtil.nullSafeAnnotations(user).put(User.EMAIL_TO_VERIFY, "optional@example.com");
        allowRateLimiter("verify-email-" + USERNAME);
        when(emailVerificationService.verify(USERNAME, "654321")).thenReturn(Mono.error(new EmailVerificationFailed()));

        submit("email=optional%40example.com&emailCode=654321")
                .exchange()
                .expectStatus()
                .isOk();

        assertFieldError("emailCode", "form.error.codeInvalid");
        assertThat(user.getSpec().getEmail()).isEqualTo("Initial@Example.com");
        assertThat(user.getSpec().isEmailVerified()).isFalse();
        verify(emailVerificationService).verify(USERNAME, "654321");
        verify(client, never()).update(any(User.class));
    }

    private WebTestClient.RequestHeadersSpec<?> authenticatedGet() {
        return webClient.get().uri("/complete-profile").header(TEST_USERNAME_HEADER, USERNAME);
    }

    private WebTestClient.RequestHeadersSpec<?> sendCode(String email) {
        return authenticatedJsonPost("/complete-profile/send-email-code", "{\"email\":\"" + email + "\"}");
    }

    private WebTestClient.RequestHeadersSpec<?> authenticatedJsonPost(String path, String body) {
        return webClient
                .post()
                .uri(path)
                .header(TEST_USERNAME_HEADER, USERNAME)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    private WebTestClient.RequestHeadersSpec<?> submit(String body) {
        return webClient
                .post()
                .uri("/complete-profile")
                .header(TEST_USERNAME_HEADER, USERNAME)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body);
    }

    private void assertFieldError(String field, String code) {
        assertThat(renderedView.get()).isEqualTo("complete_profile");
        assertThat(renderedModel.get())
                .containsEntry("mustVerifyEmailOnRegistration", setting.isMustVerifyEmailOnRegistration());
        var bindingResult = (BindingResult) renderedModel.get().get(BindingResult.MODEL_KEY_PREFIX + "form");
        assertThat(bindingResult.getFieldError(field))
                .isNotNull()
                .extracting("code")
                .isEqualTo(code);
    }

    private void assertVerifiedUserDidNotAct() {
        verify(userService, never()).checkEmailAlreadyVerified(anyString());
        verifyNoInteractions(emailVerificationService, rateLimiterRegistry, client);
    }

    private void allowRateLimiter(String key) {
        var rateLimiter = RateLimiterRegistry.ofDefaults().rateLimiter(key);
        when(rateLimiterRegistry.rateLimiter(key, rateLimiterConfigName(key))).thenReturn(rateLimiter);
    }

    private void exhaustRateLimiter(String key) {
        var config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(1)
                .timeoutDuration(Duration.ZERO)
                .build();
        var rateLimiter = RateLimiter.of(key, config);
        assertThat(rateLimiter.acquirePermission()).isTrue();
        when(rateLimiterRegistry.rateLimiter(key, rateLimiterConfigName(key))).thenReturn(rateLimiter);
    }

    private static String rateLimiterConfigName(String key) {
        return key.startsWith("verify-email-") ? "verify-email" : "send-email-verification-code";
    }

    private static User user(String email, boolean emailVerified) {
        var user = new User();
        var metadata = new Metadata();
        metadata.setName(USERNAME);
        user.setMetadata(metadata);
        user.getSpec().setEmail(email);
        user.getSpec().setEmailVerified(emailVerified);
        return user;
    }

    private static WebFilter testAuthenticationFilter() {
        return (exchange, chain) -> {
            var username = exchange.getRequest().getHeaders().getFirst(TEST_USERNAME_HEADER);
            var result = chain.filter(exchange);
            if (username == null) {
                return result;
            }
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    username, "password", List.of(new SimpleGrantedAuthority("ROLE_authenticated")));
            return result.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        };
    }

    private static org.springframework.security.web.server.SecurityWebFilterChain authorizationFilterChain() {
        var http = ServerHttpSecurity.http();
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable);
        productionAuthorizationConfigurer("authenticatedAuthorizationConfigurer")
                .configure(http);
        productionAuthorizationConfigurer("permitAllAuthorizationConfigurer").configure(http);
        return http.build();
    }

    private static SecurityConfigurer productionAuthorizationConfigurer(String methodName) {
        try {
            var type = Class.forName("run.halo.app.security.authorization.AuthorizationExchangeConfigurers");
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            var configurers = constructor.newInstance();
            var method = type.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return (SecurityConfigurer) method.invoke(configurers);
        } catch (ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e) {
            throw new IllegalStateException("Failed to load production authorization configuration", e);
        }
    }

    private static Stream<Arguments> completeProfileRequests() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/complete-profile", null, null),
                Arguments.of(
                        HttpMethod.POST,
                        "/complete-profile/send-email-code",
                        MediaType.APPLICATION_JSON,
                        "{\"email\":\"alice@example.com\"}"),
                Arguments.of(
                        HttpMethod.POST,
                        "/complete-profile",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        "email=alice%40example.com&emailCode=123456"));
    }
}
