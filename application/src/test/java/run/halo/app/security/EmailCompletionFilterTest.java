package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

class EmailCompletionFilterTest {

    private static final String USERNAME = "alice";

    private SystemConfigFetcher systemConfigFetcher;
    private UserService userService;
    private ServerRequestCache requestCache;
    private WebFilterChain chain;
    private EmailCompletionFilter filter;

    @BeforeEach
    void setUp() {
        systemConfigFetcher = mock(SystemConfigFetcher.class);
        userService = mock(UserService.class);
        requestCache = mock(ServerRequestCache.class);
        chain = mock(WebFilterChain.class);
        filter = new EmailCompletionFilter(systemConfigFetcher, userService, requestCache, responseContext());
    }

    @Test
    void shouldPassWhenAuthenticationIsMissing() {
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));
        pass(exchange, null);

        verifyNoInteractions(systemConfigFetcher, userService, requestCache);
    }

    @Test
    void shouldPassAnonymousAuthentication() {
        var authentication = new AnonymousAuthenticationToken(
                "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_anonymous")));
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));
        pass(exchange, authentication);

        verifyNoInteractions(systemConfigFetcher, userService, requestCache);
    }

    @Test
    void shouldGateRememberMeAuthenticationWhenUserIsIncomplete() {
        var authentication = new RememberMeAuthenticationToken(
                "key", USERNAME, List.of(new SimpleGrantedAuthority("ROLE_authenticated")));
        var exchange = exchange(MockServerHttpRequest.get("/apis/api.console.halo.run/v1alpha1/users")
                .accept(MediaType.APPLICATION_JSON));
        requireEmailVerification();
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user("alice@example.com", false)));

        assertBlockedWithProblemDetail(exchange, authentication);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPassWhenVerificationSettingIsAbsentOrDisabled(boolean settingPresent) {
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(settingPresent ? Mono.just(userSetting(false)) : Mono.empty());

        pass(exchange, authenticatedUser());

        verifyNoInteractions(userService, requestCache);
    }

    @Test
    void shouldPassVerifiedUser() {
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));
        requireEmailVerification();
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user("alice@example.com", true)));

        pass(exchange, authenticatedUser());

        verifyNoInteractions(requestCache);
    }

    @ParameterizedTest
    @MethodSource("incompleteUsers")
    void shouldBlockIncompleteUser(String email) {
        var exchange = exchange(MockServerHttpRequest.get("/apis/api.console.halo.run/v1alpha1/users")
                .accept(MediaType.APPLICATION_JSON));
        requireEmailVerification();
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(email, false)));

        assertBlockedWithProblemDetail(exchange, authenticatedUser());
    }

    @Test
    void shouldPassSuperRoleWithoutLoadingUser() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                USERNAME, "password", List.of(new SimpleGrantedAuthority("ROLE_super-role")));
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));

        pass(exchange, authentication);

        verifyNoInteractions(systemConfigFetcher, userService, requestCache);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/oauth2/authorization/github",
                "/login",
                "/login/oauth2/register",
                "/signup",
                "/password-reset/email/send",
                "/logout",
                "/complete-profile",
                "/complete-profile/send-email-code",
                "/system/setup",
                "/error",
                "/ui-assets/app.js",
                "/themes/default/assets/app.css",
                "/themes/default/ui-plugin/assets/app.js",
                "/themes/default/screenshot.png",
                "/plugins/example/assets/app.js",
                "/webjars/example/app.js",
                "/js/main.js",
                "/styles/main.css",
                "/halo-tracker.js",
                "/images/logo.png",
                "/favicon.ico"
            })
    void shouldPassExemptPath(String path) {
        var exchange = exchange(MockServerHttpRequest.get(path).accept(MediaType.TEXT_HTML));

        pass(exchange, authenticatedUser());

        verifyNoInteractions(systemConfigFetcher, userService, requestCache);
    }

    @Test
    void shouldNotExemptNonGetStaticResource() {
        var exchange = exchange(MockServerHttpRequest.method(HttpMethod.POST, "/ui-assets/app.js")
                .accept(MediaType.APPLICATION_JSON));
        requireEmailVerification();
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user("alice@example.com", false)));

        assertBlockedWithProblemDetail(exchange, authenticatedUser());
    }

    @Test
    void shouldSaveAndRedirectNavigationalHtmlRequest() {
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));
        requireEmailVerification();
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user("alice@example.com", false)));
        when(requestCache.saveRequest(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticatedUser()))
                .as(StepVerifier::create)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/complete-profile"));
        verify(requestCache).saveRequest(exchange);
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldReturnProblemDetailForXhrAcceptingHtml() {
        var exchange = exchange(MockServerHttpRequest.get("/archives")
                .accept(MediaType.TEXT_HTML)
                .header("X-Requested-With", "XMLHttpRequest"));
        requireEmailVerification();
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user("alice@example.com", false)));

        assertBlockedWithProblemDetail(exchange, authenticatedUser());
        verifyNoInteractions(requestCache);
    }

    @ParameterizedTest
    @MethodSource("nonHtmlRequests")
    void shouldReturnExactProblemDetailForJsonOrXhrRequest(MockServerHttpRequest.BaseBuilder<?> request) {
        var exchange = exchange(request);
        requireEmailVerification();
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user("alice@example.com", false)));

        assertBlockedWithProblemDetail(exchange, authenticatedUser());
        verifyNoInteractions(requestCache);
    }

    @Test
    void shouldInstallFilterAfterAnonymousAuthentication() {
        var http = mock(ServerHttpSecurity.class);

        new EmailCompletionSecurityConfigurer(systemConfigFetcher, userService, requestCache, responseContext())
                .configure(http);

        verify(http)
                .addFilterAfter(any(EmailCompletionFilter.class), eq(SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION));
    }

    @Test
    void shouldNotRegisterFilterAsGlobalWebFilter() {
        assertThat(AnnotatedElementUtils.hasAnnotation(EmailCompletionFilter.class, Component.class))
                .isFalse();
    }

    private void pass(MockServerWebExchange exchange, Authentication authentication) {
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        var result = filter.filter(exchange, chain);
        if (authentication != null) {
            result = result.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        }
        result.as(StepVerifier::create).verifyComplete();
        verify(chain).filter(exchange);
    }

    private void assertBlockedWithProblemDetail(MockServerWebExchange exchange, Authentication authentication) {
        var body = filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                .then(Mono.defer(exchange.getResponse()::getBodyAsString))
                .block();

        assertProblemDetail(exchange, body);
        verify(chain, never()).filter(any());
    }

    private void requireEmailVerification() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(true)));
    }

    private static Authentication authenticatedUser() {
        return UsernamePasswordAuthenticationToken.authenticated(
                USERNAME, "password", List.of(new SimpleGrantedAuthority("ROLE_authenticated")));
    }

    private static User user(String email, boolean emailVerified) {
        var user = new User();
        user.getSpec().setEmail(email);
        user.getSpec().setEmailVerified(emailVerified);
        return user;
    }

    private static SystemSetting.User userSetting(boolean mustVerifyEmail) {
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(mustVerifyEmail);
        return setting;
    }

    private static Stream<String> incompleteUsers() {
        return Stream.of("alice@example.com", null);
    }

    private static Stream<Arguments> nonHtmlRequests() {
        return Stream.of(
                Arguments.of(MockServerHttpRequest.get("/apis/api.console.halo.run/v1alpha1/users")
                        .accept(MediaType.APPLICATION_JSON)),
                Arguments.of(MockServerHttpRequest.get("/archives").header("X-Requested-With", "XMLHttpRequest")));
    }

    private static MockServerWebExchange exchange(MockServerHttpRequest.BaseBuilder<?> request) {
        return MockServerWebExchange.from(request);
    }

    private static ServerResponse.Context responseContext() {
        var strategies = HandlerStrategies.withDefaults();
        return new ServerResponse.Context() {
            @Override
            public List<org.springframework.http.codec.HttpMessageWriter<?>> messageWriters() {
                return strategies.messageWriters();
            }

            @Override
            public List<org.springframework.web.reactive.result.view.ViewResolver> viewResolvers() {
                return strategies.viewResolvers();
            }
        };
    }

    private static void assertProblemDetail(MockServerWebExchange exchange, String body) {
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(body).contains("\"type\":\"email-not-set\"");
        assertThat(body).contains("\"detail\":\"A verified email address is required.\"");
    }
}
