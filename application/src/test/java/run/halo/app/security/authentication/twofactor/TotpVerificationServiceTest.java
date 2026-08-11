package run.halo.app.security.authentication.twofactor;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.extension.Metadata;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

@ExtendWith(MockitoExtension.class)
class TotpVerificationServiceTest {

    @Mock
    TotpAuthService totpAuthService;

    @InjectMocks
    TotpVerificationService service;

    User user(String encryptedSecret) {
        var spec = new UserSpec();
        spec.setTotpEncryptedSecret(encryptedSecret);
        var user = new User();
        user.setSpec(spec);
        user.setMetadata(new Metadata());
        return user;
    }

    @Test
    void shouldPassWhenTotpNotConfigured() {
        StepVerifier.create(service.validate(user(null), null)).verifyComplete();
    }

    @Test
    void shouldFailWhenCodeMissing() {
        StepVerifier.create(service.validate(user("encrypted"), null))
                .expectError(ServerWebInputException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeNotNumeric() {
        StepVerifier.create(service.validate(user("encrypted"), "abc"))
                .expectError(ServerWebInputException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeInvalid() {
        when(totpAuthService.decryptSecret("encrypted")).thenReturn("raw-secret");
        when(totpAuthService.validateTotp("raw-secret", 123456)).thenReturn(false);
        StepVerifier.create(service.validate(user("encrypted"), "123456"))
                .expectError(ServerWebInputException.class)
                .verify();
    }

    @Test
    void shouldPassWhenCodeValid() {
        when(totpAuthService.decryptSecret("encrypted")).thenReturn("raw-secret");
        when(totpAuthService.validateTotp("raw-secret", 123456)).thenReturn(true);
        StepVerifier.create(service.validate(user("encrypted"), "123456")).verifyComplete();
    }
}
