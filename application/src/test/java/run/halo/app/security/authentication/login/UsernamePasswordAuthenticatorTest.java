package run.halo.app.security.authentication.login;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import reactor.test.StepVerifier;

/**
 * Tests for {@link UsernamePasswordAuthenticator}.
 *
 * @author guqing
 * @since 2.6.0
 */
@ExtendWith(MockitoExtension.class)
class UsernamePasswordAuthenticatorTest {

    @Mock
    private ReactiveUserDetailsPasswordService userDetailsPasswordService;

    @Mock
    private ReactiveUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ServerSecurityContextRepository serverSecurityContextRepository;

    @InjectMocks
    private UsernamePasswordAuthenticator authenticator;

    @Nested
    class RedirectUriFromRequest {
        private UsernamePasswordAuthenticator.LoginSuccessHandler loginSuccessHandler;

        @BeforeEach
        void setUp() {
            loginSuccessHandler =
                authenticator.new LoginSuccessHandler();
        }

        @Test
        void validRedirectUri() {
            // Mock a request with a valid redirect_uri
            MockServerHttpRequest request1 = MockServerHttpRequest
                .get("/some/path?redirect_uri=http://localhost:8080/redirect")
                .build();
            MockServerWebExchange exchange1 = MockServerWebExchange.from(request1);
            // Verify that the method returns the correct redirect_uri
            StepVerifier.create(loginSuccessHandler.getRedirectUri(exchange1))
                .expectComplete()
                .verify();

            request1 = MockServerHttpRequest
                .get("/some/path?redirect_uri=/console/")
                .build();
            exchange1 = MockServerWebExchange.from(request1);
            // Verify that the method returns the correct redirect_uri
            StepVerifier.create(loginSuccessHandler.getRedirectUri(exchange1))
                .expectNext(URI.create("/console/"))
                .expectComplete()
                .verify();
        }

        @Test
        void noRedirectUri() {
            // Mock a request with no redirect_uri
            MockServerHttpRequest request2 = MockServerHttpRequest
                .get("/some/path")
                .build();
            MockServerWebExchange exchange2 = MockServerWebExchange.from(request2);
            // Verify that the method returns empty
            StepVerifier.create(loginSuccessHandler.getRedirectUri(exchange2))
                .expectComplete()
                .verify();
        }

        @Test
        void differentHostRedirectUri() {
            // Mock a request with a redirect_uri pointing to a different host
            MockServerHttpRequest request3 = MockServerHttpRequest
                .get("/some/path?redirect_uri=http://example.com/redirect")
                .build();
            MockServerWebExchange exchange3 = MockServerWebExchange.from(request3);
            // Verify that the method returns empty
            StepVerifier.create(loginSuccessHandler.getRedirectUri(exchange3))
                .expectComplete()
                .verify();
        }
    }
}
