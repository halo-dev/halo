package run.halo.app.security.authentication.emailcode;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.security.authentication.exception.TooManyRequestsException;

/**
 * Tests for {@link EmailCodeLoginAuthenticationConverter}.
 *
 * @author johnniang
 * @since 2.26.0
 */
@ExtendWith(MockitoExtension.class)
class EmailCodeLoginAuthenticationConverterTest {

    @Mock
    ServerWebExchange exchange;

    @Mock
    RateLimiterRegistry rateLimiterRegistry;

    MultiValueMap<String, String> formData;

    EmailCodeLoginAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        formData = new LinkedMultiValueMap<>();
        lenient().when(exchange.getFormData()).thenReturn(Mono.just(formData));
        var request = mock(ServerHttpRequest.class);
        var headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(rateLimiterRegistry.rateLimiter("verify-login-email-code-from-unknown", "verify-login-email-code"))
                .thenReturn(RateLimiter.ofDefaults("verify-login-email-code"));
        converter = new EmailCodeLoginAuthenticationConverter(rateLimiterRegistry);
    }

    @Test
    void shouldExtractEmailAndCodeFromFormData() {
        formData.add("email", "test@example.com");
        formData.add("code", "123456");

        StepVerifier.create(converter.convert(exchange))
                .assertNext(authentication -> {
                    var token = (EmailCodeAuthenticationToken) authentication;
                    assertTrue(token.getPrincipal().toString().equals("test@example.com"));
                    assertTrue(token.getCredentials().toString().equals("123456"));
                    assertTrue(!token.isAuthenticated());
                })
                .verifyComplete();
    }

    @Test
    void shouldTriggerRateLimit() {
        formData.add("email", "test@example.com");
        formData.add("code", "123456");
        var rateLimiter = RateLimiter.of(
                "verify-login-email-code",
                RateLimiterConfig.custom()
                        .limitForPeriod(1)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofMillis(0))
                        .build());
        assertTrue(rateLimiter.acquirePermission(1));
        when(rateLimiterRegistry.rateLimiter("verify-login-email-code-from-unknown", "verify-login-email-code"))
                .thenReturn(rateLimiter);

        StepVerifier.create(converter.convert(exchange))
                .expectError(TooManyRequestsException.class)
                .verify();
    }

    @Test
    void shouldFailWhenEmailMissing() {
        formData.add("code", "123456");

        StepVerifier.create(converter.convert(exchange))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeMissing() {
        formData.add("email", "test@example.com");

        StepVerifier.create(converter.convert(exchange))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
