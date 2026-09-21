package run.halo.app.security.sudo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.ReasonPayload;

@ExtendWith(MockitoExtension.class)
class EmailSudoVerificationProviderTest {

    @Mock
    NotificationReasonEmitter reasonEmitter;

    @Mock
    NotificationCenter notificationCenter;

    InMemorySudoCodeStore codeStore;

    EmailSudoVerificationProvider provider;

    @BeforeEach
    void setUp() {
        codeStore = new InMemorySudoCodeStore();
        provider = new EmailSudoVerificationProvider(codeStore, reasonEmitter, notificationCenter);
    }

    @Test
    void shouldSupportVerifiedEmailOnly() {
        StepVerifier.create(provider.supports(user(true, "a@example.com")))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(provider.supports(user(false, "a@example.com")))
                .expectNext(false)
                .verifyComplete();
        StepVerifier.create(provider.supports(user(true, null)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldMaskEmail() {
        assertThat(provider.maskedTarget(user(true, "alice@example.com"))).isEqualTo("a***@example.com");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendAndVerifyCode() {
        when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.empty());
        when(reasonEmitter.emit(eq(EmailSudoVerificationProvider.SUDO_EMAIL_CODE_REASON_TYPE), any(Consumer.class)))
                .thenReturn(Mono.empty());

        var user = user(true, "alice@example.com");
        StepVerifier.create(provider.sendCode(user)).verifyComplete();

        var captor = ArgumentCaptor.forClass(Consumer.class);
        verify(reasonEmitter).emit(eq(EmailSudoVerificationProvider.SUDO_EMAIL_CODE_REASON_TYPE), captor.capture());
        var builder = ReasonPayload.builder();
        captor.getValue().accept(builder);
        var code = builder.build().getAttributes().get("code").toString();

        StepVerifier.create(provider.verify(user, code)).verifyComplete();
    }

    @Test
    void shouldNotSendWhenStoreNotTouchedForUnsupportedUser() {
        StepVerifier.create(provider.supports(user(false, "alice@example.com")))
                .expectNext(false)
                .verifyComplete();
        verify(reasonEmitter, never()).emit(any(), any());
    }

    private static User user(boolean verified, String email) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("alice");
        user.getSpec().setEmailVerified(verified);
        user.getSpec().setEmail(email);
        return user;
    }
}
