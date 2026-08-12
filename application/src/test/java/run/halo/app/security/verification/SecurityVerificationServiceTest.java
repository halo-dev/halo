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
    void shouldHaveVerificationMethodWhenEmailVerified() {
        assertThat(service.hasVerificationMethod(user(true, null))).isTrue();
    }

    @Test
    void shouldHaveVerificationMethodWhenTotpConfigured() {
        assertThat(service.hasVerificationMethod(user(false, "encrypted-secret")))
                .isTrue();
    }

    @Test
    void shouldNotHaveVerificationMethodWithoutAnyMethod() {
        assertThat(service.hasVerificationMethod(user(false, null))).isFalse();
    }

    @Test
    void shouldListEmailWhenEmailVerified() {
        assertThat(service.availableMethods(user(true, null)))
                .extracting(SecurityVerificationService.SecurityVerificationMethod::name)
                .containsExactly("email");
    }

    @Test
    void shouldListMethodsInDisplayOrder() {
        assertThat(service.availableMethods(user(true, "encrypted-secret")))
                .extracting(SecurityVerificationService.SecurityVerificationMethod::name)
                .containsExactly("email", "totp");
    }

    @Test
    void shouldListOnlyTotpWhenEmailNotVerified() {
        assertThat(service.availableMethods(user(false, "encrypted-secret")))
                .extracting(SecurityVerificationService.SecurityVerificationMethod::name)
                .containsExactly("totp");
    }
}
