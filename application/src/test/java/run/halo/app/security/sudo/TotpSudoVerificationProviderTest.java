package run.halo.app.security.sudo;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TotpSudoVerificationProviderTest {

    @Mock
    TotpAuthService totpAuthService;

    TotpSudoVerificationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new TotpSudoVerificationProvider(totpAuthService);
    }

    @Test
    void shouldSupportWhenTotpConfigured() {
        StepVerifier.create(provider.supports(userWithSecret("secret")))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(provider.supports(userWithSecret(null)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldRejectSendCode() {
        StepVerifier.create(provider.sendCode(userWithSecret("secret")))
                .expectError(ServerWebInputException.class)
                .verify();
    }

    @Test
    void shouldVerifyValidTotp() {
        when(totpAuthService.decryptSecret("encrypted")).thenReturn("raw");
        when(totpAuthService.validateTotp("raw", 123456)).thenReturn(true);
        StepVerifier.create(provider.verify(userWithSecret("encrypted"), "123456"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTotpInvalid() {
        when(totpAuthService.decryptSecret("encrypted")).thenReturn("raw");
        when(totpAuthService.validateTotp("raw", 123456)).thenReturn(false);
        StepVerifier.create(provider.verify(userWithSecret("encrypted"), "123456"))
                .expectError(SudoVerificationFailedException.class)
                .verify();
    }

    @Test
    void shouldFailWhenTotpNotNumeric() {
        StepVerifier.create(provider.verify(userWithSecret("encrypted"), "abcdef"))
                .expectError(SudoVerificationFailedException.class)
                .verify();
    }

    private static User userWithSecret(String secret) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("alice");
        user.getSpec().setTotpEncryptedSecret(secret);
        return user;
    }
}
