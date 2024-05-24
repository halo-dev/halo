package run.halo.app.security.authentication.login;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.security.authentication.CryptoService;

@ExtendWith(MockitoExtension.class)
class LoginAuthenticationConverterTest {

    @Mock
    ServerWebExchange exchange;

    @Mock
    CryptoService cryptoService;

    @Mock
    RateLimiterRegistry rateLimiterRegistry;

    @InjectMocks
    LoginAuthenticationConverter converter;

    MultiValueMap<String, String> formData;

    @BeforeEach
    void setUp() {
        formData = new LinkedMultiValueMap<>();
        lenient().when(exchange.getFormData()).thenReturn(Mono.just(formData));
        var request = mock(ServerHttpRequest.class);
        var headers = new HttpHeaders();

        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(rateLimiterRegistry.rateLimiter("authentication-from-ip-unknown",
            "authentication"))
            .thenReturn(RateLimiter.ofDefaults("authentication"));
    }

    @Test
    void shouldTriggerRateLimit() {
        var username = "username";
        var password = "password";

        formData.add("username", username);
        formData.add("password", Base64.getEncoder().encodeToString(password.getBytes()));
        var rateLimiter = RateLimiter.of("authentication", RateLimiterConfig.custom()
            .limitForPeriod(1)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(0))
            .build());
        assertTrue(rateLimiter.acquirePermission(1));
        when(rateLimiterRegistry.rateLimiter("authentication-from-ip-unknown", "authentication"))
            .thenReturn(rateLimiter);
        StepVerifier.create(converter.convert(exchange))
            .expectError(RateLimitExceededException.class)
            .verify();

        verify(cryptoService, never()).decrypt(password.getBytes());
    }

    @Test
    void applyUsernameAndPasswordThenCreatesTokenSuccess() {
        var username = "username";
        var password = "password";
        var decryptedPassword = "decrypted password";

        formData.add("username", username);
        formData.add("password", Base64.getEncoder().encodeToString(password.getBytes()));

        when(cryptoService.decrypt(password.getBytes()))
            .thenReturn(Mono.just(decryptedPassword.getBytes()));
        StepVerifier.create(converter.convert(exchange))
            .expectNext(new UsernamePasswordAuthenticationToken(username, decryptedPassword))
            .verifyComplete();

        verify(cryptoService).decrypt(password.getBytes());
    }

    @Test
    void applyPasswordWithoutBase64FormatThenBadCredentialsException() {
        var username = "username";
        var password = "+invalid-base64-format-password";

        formData.add("username", username);
        formData.add("password", password);

        StepVerifier.create(converter.convert(exchange))
            .verifyError(BadCredentialsException.class);
    }

    @Test
    void applyUsernameAndInvalidPasswordThenBadCredentialsException() {
        var username = "username";
        var password = "password";

        formData.add("username", username);
        formData.add("password", Base64.getEncoder().encodeToString(password.getBytes()));

        when(cryptoService.decrypt(password.getBytes()))
            .thenReturn(Mono.error(() -> new InvalidEncryptedMessageException("invalid message")));
        StepVerifier.create(converter.convert(exchange))
            .verifyError(BadCredentialsException.class);
        verify(cryptoService).decrypt(password.getBytes());
    }

}