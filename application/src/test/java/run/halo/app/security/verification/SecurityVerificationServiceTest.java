package run.halo.app.security.verification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebSession;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.extension.Metadata;

class SecurityVerificationServiceTest {

    final SecurityVerificationService service = new SecurityVerificationService();

    WebSession session() {
        return MockServerWebExchange.builder(MockServerHttpRequest.get("/"))
                .build()
                .getSession()
                .block();
    }

    User user(boolean emailVerified, String totpSecret) {
        var spec = new UserSpec();
        spec.setEmailVerified(emailVerified);
        spec.setTotpEncryptedSecret(totpSecret);
        var user = new User();
        user.setSpec(spec);
        user.setMetadata(new Metadata());
        return user;
    }

    @Test
    void shouldNotBeVerifiedInitially() {
        assertThat(service.isVerified(session())).isFalse();
    }

    @Test
    void shouldBeVerifiedAfterMarking() {
        var session = session();
        service.markVerified(session);
        assertThat(service.isVerified(session)).isTrue();
    }

    @Test
    void shouldExpireAfterTtl() {
        var session = session();
        session.getAttributes()
                .put(
                        SecurityVerificationService.VERIFIED_AT_SESSION_KEY,
                        Instant.now().minus(SecurityVerificationService.VERIFICATION_TTL.plus(Duration.ofSeconds(1))));
        assertThat(service.isVerified(session)).isFalse();
    }

    @Test
    void shouldBeAvailableWhenEmailVerified() {
        assertThat(service.isAvailable(user(true, null))).isTrue();
    }

    @Test
    void shouldBeAvailableWhenTotpConfigured() {
        assertThat(service.isAvailable(user(false, "encrypted-secret"))).isTrue();
    }

    @Test
    void shouldNotBeAvailableWithoutAnyMethod() {
        assertThat(service.isAvailable(user(false, null))).isFalse();
    }
}
