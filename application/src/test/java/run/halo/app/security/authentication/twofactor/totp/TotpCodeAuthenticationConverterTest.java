package run.halo.app.security.authentication.twofactor.totp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.security.authentication.exception.TooManyRequestsException;
import run.halo.app.security.authentication.exception.TwoFactorAuthException;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

@ExtendWith(MockitoExtension.class)
class TotpCodeAuthenticationConverterTest {

    @Mock
    ServerWebExchange exchange;

    @Mock
    RateLimiterRegistry rateLimiterRegistry;

    @InjectMocks
    TotpCodeAuthenticationConverter converter;

    MultiValueMap<String, String> formData;

    MultiValueMap<String, HttpCookie> cookies;

    HttpHeaders headers;

    TwoFactorAuthentication twoFactorAuthentication;

    @BeforeEach
    void setUp() {
        formData = new LinkedMultiValueMap<>();
        cookies = new LinkedMultiValueMap<>();
        headers = new HttpHeaders();

        var request = mock(ServerHttpRequest.class);
        lenient().when(request.getCookies()).thenReturn(cookies);
        lenient().when(request.getHeaders()).thenReturn(headers);

        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getFormData()).thenReturn(Mono.just(formData));

        // Default rate limiter that always permits — individual tests override.
        lenient()
                .when(rateLimiterRegistry.rateLimiter(org.mockito.ArgumentMatchers.anyString(), eq("totp-validation")))
                .thenReturn(RateLimiter.ofDefaults("totp-validation"));

        // The converter only proceeds if the request is wrapped in a TwoFactorAuthentication.
        twoFactorAuthentication = new TwoFactorAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("fakeuser", "n/a"));
    }

    /**
     * Drive the converter inside a reactor context that already holds a {@link TwoFactorAuthentication}, mirroring how
     * the security filter chain wires it.
     */
    private Mono<?> runConvert() {
        return converter
                .convert(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(twoFactorAuthentication));
    }

    @Test
    void shouldEmitTokenForValidCode() {
        formData.add("code", "123456");

        StepVerifier.create(runConvert())
                .assertNext(auth -> {
                    assertTrue(auth instanceof TotpAuthenticationToken);
                    assertEquals(123456, ((TotpAuthenticationToken) auth).getCredentials());
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectBlankCodeWithTwoFactorAuthException() {
        formData.add("code", "   ");

        StepVerifier.create(runConvert())
                .expectError(TwoFactorAuthException.class)
                .verify();
    }

    @Test
    void shouldRejectMissingCodeWithTwoFactorAuthException() {
        // formData stays empty — no `code` parameter.
        StepVerifier.create(runConvert())
                .expectError(TwoFactorAuthException.class)
                .verify();
    }

    @Test
    void shouldRejectNonNumericCodeWithTwoFactorAuthException() {
        formData.add("code", "abc123");

        StepVerifier.create(runConvert())
                .expectError(TwoFactorAuthException.class)
                .verify();
    }

    @Test
    void shouldRejectWhenMfaContextMissing() {
        formData.add("code", "123456");

        // Drive the convert() chain WITHOUT a TwoFactorAuthentication in context.
        StepVerifier.create(converter.convert(exchange))
                .expectError(TwoFactorAuthException.class)
                .verify();
    }

    @Test
    void shouldMapRequestNotPermittedToTooManyRequestsException() {
        formData.add("code", "123456");

        // Consumed-only rate limiter: 1 permit/sec, already exhausted.
        var rateLimiter = RateLimiter.of(
                "totp-validation",
                RateLimiterConfig.custom()
                        .limitForPeriod(1)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofMillis(0))
                        .build());
        assertTrue(rateLimiter.acquirePermission(1));

        when(rateLimiterRegistry.rateLimiter(org.mockito.ArgumentMatchers.anyString(), eq("totp-validation")))
                .thenReturn(rateLimiter);

        StepVerifier.create(runConvert())
                .expectError(TooManyRequestsException.class)
                .verify();
    }

    @Test
    void shouldDeriveRateLimitKeyFromSessionCookieWhenPresent() {
        formData.add("code", "123456");
        cookies.add("SESSION", new HttpCookie("SESSION", "session-token-abc"));

        StepVerifier.create(runConvert()).expectNextCount(1).verifyComplete();

        var keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rateLimiterRegistry).rateLimiter(keyCaptor.capture(), eq("totp-validation"));
        assertEquals("totp-validation-session-token-abc", keyCaptor.getValue());
    }

    @Test
    void shouldFallBackToClientIpKeyWhenSessionCookieMissing() {
        formData.add("code", "123456");
        // cookies left empty — no SESSION cookie.
        headers.add("X-Forwarded-For", "203.0.113.42");

        StepVerifier.create(runConvert()).expectNextCount(1).verifyComplete();

        var keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rateLimiterRegistry).rateLimiter(keyCaptor.capture(), eq("totp-validation"));
        assertEquals("totp-validation-203.0.113.42", keyCaptor.getValue());
    }

    @Test
    void shouldFallBackToUnknownKeyWhenNoSessionNoIpHeader() {
        formData.add("code", "123456");
        // cookies + headers both empty — IpAddressUtils returns "unknown".

        StepVerifier.create(runConvert()).expectNextCount(1).verifyComplete();

        var keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rateLimiterRegistry).rateLimiter(keyCaptor.capture(), eq("totp-validation"));
        assertEquals("totp-validation-unknown", keyCaptor.getValue());
    }
}
