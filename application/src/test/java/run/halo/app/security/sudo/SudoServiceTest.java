package run.halo.app.security.sudo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SudoServiceTest {

    @Mock
    UserService userService;

    @Mock
    RateLimiterRegistry rateLimiterRegistry;

    @Mock
    SudoVerificationProvider totpProvider;

    @Mock
    SudoVerificationProvider emailProvider;

    SudoService sudoService;

    Clock clock;

    @BeforeEach
    void setUp() {
        when(totpProvider.method()).thenReturn("totp");
        when(emailProvider.method()).thenReturn("email");
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString()))
                .thenAnswer(invocation -> RateLimiter.ofDefaults(invocation.getArgument(0)));
        sudoService = new SudoService(List.of(totpProvider, emailProvider), userService, rateLimiterRegistry);
        clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        sudoService.setClock(clock);
    }

    @Test
    void shouldListSupportedMethods() {
        var user = user(true, true);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));
        when(totpProvider.supports(user)).thenReturn(Mono.just(true));
        when(totpProvider.sendable()).thenReturn(false);
        when(emailProvider.supports(user)).thenReturn(Mono.just(true));
        when(emailProvider.sendable()).thenReturn(true);
        when(emailProvider.maskedTarget(user)).thenReturn("a***@example.com");

        StepVerifier.create(sudoService.availableMethods("alice"))
                .assertNext(methods -> {
                    assertThat(methods).extracting(SudoMethod::name).containsExactly("totp", "email");
                    assertThat(methods.get(1).maskedTarget()).isEqualTo("a***@example.com");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectConfirmForUnsupportedMethodWithoutCallingVerify() {
        var user = user(true, false);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));
        when(totpProvider.supports(user)).thenReturn(Mono.just(true));
        when(emailProvider.supports(user)).thenReturn(Mono.just(false));

        var exchange = exchange();
        StepVerifier.create(sudoService
                        .confirm("email", "123456", exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .expectError(ServerWebInputException.class)
                .verify();

        verify(emailProvider, never()).verify(user, "123456");
        verify(totpProvider, never()).verify(user, "123456");
    }

    @Test
    void shouldActivateSessionAfterSuccessfulConfirm() {
        var user = user(true, false);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));
        when(totpProvider.supports(user)).thenReturn(Mono.just(true));
        when(totpProvider.verify(user, "123456")).thenReturn(Mono.empty());

        var exchange = exchange();
        StepVerifier.create(sudoService
                        .confirm("totp", "123456", exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .verifyComplete();

        StepVerifier.create(sudoService.isActive(exchange)).expectNext(true).verifyComplete();
    }

    @Test
    void shouldNotActivateWhenVerifyFails() {
        var user = user(true, false);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));
        when(totpProvider.supports(user)).thenReturn(Mono.just(true));
        when(totpProvider.verify(user, "000000")).thenReturn(Mono.error(SudoVerificationFailedException::new));

        var exchange = exchange();
        StepVerifier.create(sudoService
                        .confirm("totp", "000000", exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .expectError(SudoVerificationFailedException.class)
                .verify();

        StepVerifier.create(sudoService.isActive(exchange)).expectNext(false).verifyComplete();
    }

    @Test
    void shouldRejectJwtForSudoApis() {
        StepVerifier.create(sudoService
                        .requireSessionAuthentication()
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth())))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void shouldSkipSudoWhenUserNotFound() {
        when(userService.getUser("alice")).thenReturn(Mono.error(new UserNotFoundException("alice")));
        StepVerifier.create(sudoService.requireSudo(exchange(), "alice")).verifyComplete();
    }

    @Test
    void shouldSkipSudoWhenNoMethodsAvailable() {
        var user = user(false, false);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));
        when(totpProvider.supports(user)).thenReturn(Mono.just(false));
        when(emailProvider.supports(user)).thenReturn(Mono.just(false));

        StepVerifier.create(sudoService.requireSudo(exchange(), "alice")).verifyComplete();
    }

    @Test
    void shouldRequireSudoWhenMethodsExistAndWindowExpired() {
        var user = user(true, false);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));
        when(totpProvider.supports(user)).thenReturn(Mono.just(true));
        when(totpProvider.sendable()).thenReturn(false);
        when(emailProvider.supports(user)).thenReturn(Mono.just(false));

        StepVerifier.create(sudoService.requireSudo(exchange(), "alice"))
                .expectError(SudoRequiredException.class)
                .verify();
    }

    @Test
    void shouldRateLimitSendCode() {
        stubExhaustedRateLimiter("send-sudo-code-alice", "send-sudo-code");

        StepVerifier.create(sudoService
                        .sendCode("email", exchange())
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .expectError(RateLimitExceededException.class)
                .verify();

        verify(userService, never()).getUser(anyString());
        verify(emailProvider, never()).sendCode(any());
    }

    @Test
    void shouldRateLimitConfirm() {
        stubExhaustedRateLimiter("sudo-confirm-alice", "sudo-confirm");

        StepVerifier.create(sudoService
                        .confirm("totp", "123456", exchange())
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .expectError(RateLimitExceededException.class)
                .verify();

        verify(userService, never()).getUser(anyString());
        verify(totpProvider, never()).verify(any(), anyString());
    }

    @Test
    void extraProviderBeanShouldAppearInMethods() {
        var extra = mock(SudoVerificationProvider.class);
        when(extra.method()).thenReturn("phone");
        when(extra.sendable()).thenReturn(true);
        sudoService = new SudoService(List.of(totpProvider, emailProvider, extra), userService, rateLimiterRegistry);
        sudoService.setClock(clock);

        var user = user(false, false);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));
        when(totpProvider.supports(user)).thenReturn(Mono.just(false));
        when(emailProvider.supports(user)).thenReturn(Mono.just(false));
        when(extra.supports(user)).thenReturn(Mono.just(true));
        when(extra.maskedTarget(user)).thenReturn("138****8000");

        StepVerifier.create(sudoService.availableMethods("alice"))
                .assertNext(methods ->
                        assertThat(methods).extracting(SudoMethod::name).containsExactly("phone"))
                .verifyComplete();
    }

    private void stubExhaustedRateLimiter(String key, String configName) {
        var rateLimiter = RateLimiter.of(
                key,
                RateLimiterConfig.custom()
                        .limitForPeriod(1)
                        .limitRefreshPeriod(Duration.ofMinutes(1))
                        .timeoutDuration(Duration.ZERO)
                        .build());
        assertThat(rateLimiter.acquirePermission()).isTrue();
        when(rateLimiterRegistry.rateLimiter(key, configName)).thenReturn(rateLimiter);
    }

    private static UsernamePasswordAuthenticationToken sessionAuth() {
        return UsernamePasswordAuthenticationToken.authenticated(
                "alice", "n/a", AuthorityUtils.createAuthorityList("ROLE_authenticated"));
    }

    private static JwtAuthenticationToken jwtAuth() {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("alice")
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    private static MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/"));
    }

    private static User user(boolean totp, boolean email) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("alice");
        if (totp) {
            user.getSpec().setTotpEncryptedSecret("secret");
        }
        user.getSpec().setEmailVerified(email);
        user.getSpec().setEmail(email ? "alice@example.com" : null);
        return user;
    }
}
