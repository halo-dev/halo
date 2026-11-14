package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.emailcode.EmailCodeService;

@ExtendWith(MockitoExtension.class)
class PreAuthEmailCodeLoginEndpointTest {

    @Mock
    EmailCodeService emailCodeService;

    @Mock
    RateLimiterRegistry rateLimiterRegistry;

    @Mock
    Validator validator;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        when(rateLimiterRegistry.rateLimiter(anyString(), eq("send-login-email-code")))
                .thenReturn(RateLimiter.ofDefaults("send-login-email-code"));
        var endpoint = new PreAuthEmailCodeLoginEndpoint(emailCodeService, rateLimiterRegistry, validator);
        webClient = WebTestClient.bindToRouterFunction(endpoint.preAuthEmailCodeLoginEndpoints())
                .build();
    }

    @Test
    void shouldReturnAcceptedWithoutResponseBody() {
        when(emailCodeService.sendLoginCode("user@example.com")).thenReturn(Mono.empty());

        webClient
                .post()
                .uri("/login/email-code/send")
                .bodyValue(Map.of("email", "user@example.com"))
                .exchange()
                .expectStatus()
                .isAccepted()
                .expectBody()
                .isEmpty();

        verify(emailCodeService).sendLoginCode("user@example.com");
    }
}
