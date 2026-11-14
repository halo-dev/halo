package run.halo.app.core.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.user.service.InMemoryResetTokenRepository;
import run.halo.app.core.user.service.InvalidResetTokenException;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.ReasonPayload;
import run.halo.app.notification.TransientEmailNotificationCoordinator;
import run.halo.app.notification.UserIdentity;

/**
 * Tests for {@link EmailPasswordRecoveryServiceImpl}.
 *
 * @author guqing
 * @since 2.11.0
 */
@ExtendWith(MockitoExtension.class)
class EmailPasswordRecoveryServiceImplTest {
    @Test
    void completingAnOlderResetDoesNotUnsubscribeANewerPendingNotification() {
        var linkProcessor = mock(ExternalLinkProcessor.class);
        var client = mock(ReactiveExtensionClient.class);
        var emitter = mock(NotificationReasonEmitter.class);
        var center = mock(NotificationCenter.class);
        var userService = mock(UserService.class);
        var tokens = new InMemoryResetTokenRepository();
        var coordinator = spy(new TransientEmailNotificationCoordinator());
        var service = new EmailPasswordRecoveryServiceImpl(
                linkProcessor, client, emitter, center, userService, tokens, coordinator);
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("alice");
        user.getSpec().setEmail("alice@example.com");
        user.getSpec().setEmailVerified(true);
        when(client.fetch(User.class, "alice")).thenReturn(Mono.just(user));
        when(linkProcessor.processLink(any(URI.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, URI.class)));
        when(center.subscribe(any(), any())).thenReturn(Mono.just(new Subscription()));
        var payloads = new ArrayList<ReasonPayload>();
        var pendingReason = Sinks.<Void>one();
        when(emitter.emit(eq("reset-password-by-email"), any())).thenAnswer(invocation -> {
            Consumer<ReasonPayload.ReasonPayloadBuilder> consumer = invocation.getArgument(1);
            var builder = ReasonPayload.builder();
            consumer.accept(builder);
            return Mono.defer(() -> {
                payloads.add(builder.build());
                return payloads.size() == 1 ? Mono.empty() : pendingReason.asMono();
            });
        });

        service.sendPasswordResetEmail("alice", "alice@example.com").block();
        var oldToken = token(payloads.getFirst());
        var newRequest =
                service.sendPasswordResetEmail("alice", "alice@example.com").subscribe();
        assertThat(payloads).hasSize(2);
        var newToken = token(payloads.getLast());
        when(userService.updateWithRawPassword("alice", "new-password")).thenReturn(Mono.just(user));

        service.changePassword("new-password", oldToken)
                .as(StepVerifier::create)
                .verifyComplete();

        verify(center, never()).unsubscribe(any(), any());
        verify(coordinator, times(2))
                .publish(
                        eq("reset-password-by-email"),
                        eq(UserIdentity.anonymousWithEmail("alice@example.com").name()),
                        any());
        service.getValidResetToken(oldToken)
                .as(StepVerifier::create)
                .expectError(InvalidResetTokenException.class)
                .verify();
        service.getValidResetToken(newToken)
                .as(StepVerifier::create)
                .assertNext(resetToken -> assertThat(resetToken.username()).isEqualTo("alice"))
                .verifyComplete();
        pendingReason.tryEmitEmpty();
        newRequest.dispose();
    }

    private String token(ReasonPayload payload) {
        var path = URI.create(payload.getAttributes().get("link").toString()).getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
