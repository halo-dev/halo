package run.halo.app.core.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static run.halo.app.core.user.service.impl.EmailVerificationServiceImpl.MAX_ATTEMPTS;

import java.util.ArrayList;
import java.util.function.Consumer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.user.service.impl.EmailVerificationServiceImpl.EmailVerificationManager.UsernameEmail;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.ReasonPayload;
import run.halo.app.notification.TransientEmailNotificationCoordinator;

/**
 * Tests for {@link EmailVerificationServiceImpl}.
 *
 * @author guqing
 * @since 2.11.0
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Test
    void serializesSubscriptionAndReasonPublicationAndKeepsLatestCodeVerifiable() {
        var client = mock(ReactiveExtensionClient.class);
        var center = mock(NotificationCenter.class);
        var emitter = mock(NotificationReasonEmitter.class);
        var service =
                new EmailVerificationServiceImpl(client, emitter, center, new TransientEmailNotificationCoordinator());
        var subscribed = Sinks.<Subscription>one();
        when(center.subscribe(any(), any())).thenReturn(subscribed.asMono(), Mono.just(new Subscription()));
        var firstReason = Sinks.<Void>one();
        var payloads = new ArrayList<ReasonPayload>();
        when(emitter.emit(eq("email-verification"), any())).thenAnswer(invocation -> {
            Consumer<ReasonPayload.ReasonPayloadBuilder> consumer = invocation.getArgument(1);
            var builder = ReasonPayload.builder();
            consumer.accept(builder);
            return Mono.defer(() -> {
                payloads.add(builder.build());
                return payloads.size() == 1 ? firstReason.asMono() : Mono.empty();
            });
        });

        var first = service.sendRegisterVerificationCode("test@example.com").subscribe();
        var second = service.sendRegisterVerificationCode("test@example.com").subscribe();
        verify(center, times(1)).subscribe(any(), any());
        assertThat(payloads).isEmpty();
        subscribed.tryEmitValue(new Subscription());
        verify(center, times(1)).subscribe(any(), any());
        assertThat(payloads).hasSize(1);

        first.dispose();
        firstReason.tryEmitEmpty();
        assertThat(payloads).hasSize(2);
        verify(center, times(2)).subscribe(any(), any());
        var latestCode = (String) payloads.get(1).getAttributes().get("code");
        service.verifyRegisterVerificationCode("test@example.com", latestCode)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
        verifyNoInteractions(client);
        second.dispose();
    }

    @Nested
    class EmailVerificationManagerTest {

        @Test
        public void generateCodeTest() {
            var emailVerificationManager = new EmailVerificationServiceImpl.EmailVerificationManager();
            emailVerificationManager.generateCode("fake-user", "fake-email");
            var result = emailVerificationManager.contains("fake-user", "fake-email");
            assertThat(result).isTrue();

            emailVerificationManager.generateCode("guqing", "hi@halo.run");
            result = emailVerificationManager.contains("guqing", "hi@halo.run");
            assertThat(result).isTrue();

            result = emailVerificationManager.contains("123", "123");
            assertThat(result).isFalse();
        }

        @Test
        public void removeTest() {
            var emailVerificationManager = new EmailVerificationServiceImpl.EmailVerificationManager();
            emailVerificationManager.generateCode("fake-user", "fake-email");
            var result = emailVerificationManager.contains("fake-user", "fake-email");
            emailVerificationManager.removeCode("fake-user", "fake-email");
            result = emailVerificationManager.contains("fake-user", "fake-email");
            assertThat(result).isFalse();
        }

        @Test
        void verifyCodeTestNormal() {
            String username = "guqing";
            String email = "hi@halo.run";
            var emailVerificationManager = new EmailVerificationServiceImpl.EmailVerificationManager();
            var result = emailVerificationManager.verifyCode(username, email, "fake-code");
            assertThat(result).isFalse();

            var code = emailVerificationManager.generateCode(username, email);
            result = emailVerificationManager.verifyCode(username, email, "fake-code");
            assertThat(result).isFalse();

            result = emailVerificationManager.verifyCode(username, email, code);
            assertThat(result).isTrue();
        }

        @Test
        void verifyCodeFailedAfterMaxAttempts() {
            String username = "guqing";
            String email = "example@example.com";
            var emailVerificationManager = new EmailVerificationServiceImpl.EmailVerificationManager();
            var code = emailVerificationManager.generateCode(username, email);
            for (int i = 0; i <= MAX_ATTEMPTS; i++) {
                var result = emailVerificationManager.verifyCode(username, email, "fake-code");
                assertThat(result).isFalse();
            }

            assertThatThrownBy(() -> emailVerificationManager.verifyCode(username, email, code))
                    .isInstanceOf(EmailVerificationFailed.class)
                    .hasMessage("400 BAD_REQUEST \"Too many attempts. Please try again later.\"");
        }
    }

    @Test
    void shouldBeEqualUsernameEmailWithDifferentCase() {
        var expected = new UsernameEmail("faker", "a@b.com");
        var got = new UsernameEmail("faker", "A@B.com");
        assertEquals(expected, got);
    }
}
