package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class TransientEmailSubscriptionCleanupTaskTest {
    private static final Instant CUTOFF = Instant.parse("2026-01-01T00:00:00Z");
    private final ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
    private final TransientEmailNotificationCoordinator coordinator = new TransientEmailNotificationCoordinator();
    private final TransientEmailSubscriptionCleanupTask task =
            new TransientEmailSubscriptionCleanupTask(client, coordinator);
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        subscription = subscription("old");
        when(client.fetch(Subscription.class, "old")).thenAnswer(ignored -> Mono.just(subscription));
    }

    @ParameterizedTest
    @ValueSource(strings = {"email-verification", "login-email-code", "sudo-email-code", "reset-password-by-email"})
    void preservesSubscriptionAcrossDelayedConsumptionAndReasonRetries(String reasonType) {
        subscription.getSpec().getReason().setReasonType(reasonType);
        when(client.listTopNames(eq(Reason.class), any(), any(), eq(1)))
                .thenReturn(Flux.just("delayed-reason"), Flux.just("retrying-reason"), Flux.empty());
        when(client.delete(subscription)).thenReturn(Mono.just(subscription));

        task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();
        task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();
        verify(client, never()).delete(any());

        task.cleanUpSubscription("old", CUTOFF)
                .as(StepVerifier::create)
                .expectNext(subscription)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"email-verification", "login-email-code", "sudo-email-code", "reset-password-by-email"})
    void skipsSubscriptionBetweenSubscribeAndReasonPublication(String reasonType) {
        subscription.getSpec().getReason().setReasonType(reasonType);
        var publishing = Sinks.<Void>one();
        coordinator
                .publish(
                        subscription.getSpec().getReason().getReasonType(),
                        subscription.getSpec().getSubscriber().getName(),
                        publishing::asMono)
                .subscribe();

        task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();

        verify(client, never()).listTopNames(any(), any(), any(), anyInt());
        verify(client, never()).delete(any());
        publishing.tryEmitEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"email-verification", "login-email-code", "sudo-email-code", "reset-password-by-email"})
    void waitsForExactOldInstanceDeletionBeforePublishingNewSubscription(String reasonType) {
        subscription.getSpec().getReason().setReasonType(reasonType);
        when(client.listTopNames(eq(Reason.class), any(), any(), eq(1))).thenReturn(Flux.empty());
        var deletion = Sinks.<Subscription>one();
        when(client.delete(subscription)).thenReturn(deletion.asMono());
        var cleanup = task.cleanUpSubscription("old", CUTOFF).subscribe();
        var publish = mock(Runnable.class);
        coordinator
                .publish(
                        subscription.getSpec().getReason().getReasonType(),
                        subscription.getSpec().getSubscriber().getName(),
                        () -> Mono.fromRunnable(publish))
                .subscribe();
        cleanup.dispose();
        verifyNoInteractions(publish);

        deletion.tryEmitValue(subscription);

        verify(publish).run();
        verify(client).delete(subscription);
        verify(client, times(2)).fetch(Subscription.class, "old");
    }

    @Test
    void skipsChangedOrMissingInstancesAndNeverRetriesConflictingDeletion() {
        var changed = subscription("old");
        changed.getMetadata().setCreationTimestamp(CUTOFF.plusSeconds(1));
        when(client.fetch(Subscription.class, "old")).thenReturn(Mono.just(subscription), Mono.just(changed));
        task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();
        verify(client, never()).delete(any());

        when(client.fetch(Subscription.class, "old")).thenReturn(Mono.just(subscription), Mono.empty());
        task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();
        verify(client, never()).delete(any());

        when(client.fetch(Subscription.class, "old")).thenReturn(Mono.just(subscription));
        when(client.listTopNames(eq(Reason.class), any(), any(), eq(1))).thenReturn(Flux.empty());
        when(client.delete(subscription)).thenReturn(Mono.error(new OptimisticLockingFailureException("changed")));
        task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();
        verify(client, times(1)).delete(subscription);
    }

    @Test
    void failsClosedWhenPendingReasonLookupFails() {
        when(client.listTopNames(eq(Reason.class), any(), any(), eq(1)))
                .thenReturn(Flux.error(new IllegalStateException("index unavailable")));
        task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();
        verify(client, never()).delete(any());
    }

    @Test
    void skipsAnInstanceWhoseReasonTypeChangedAfterAcquiringItsGuard() {
        var changed = subscription("old");
        changed.getSpec().getReason().setReasonType("login-email-code");
        when(client.fetch(Subscription.class, "old")).thenReturn(Mono.just(subscription), Mono.just(changed));
        when(client.listTopNames(eq(Reason.class), any(), any(), eq(1))).thenReturn(Flux.empty());
        when(client.delete(any(Subscription.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();

        verify(client, never()).delete(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"email-verification", "login-email-code", "sudo-email-code", "reset-password-by-email"})
    @SuppressWarnings("unchecked")
    void queuedEmailCanRetryAfterItsSubscriptionHasBeenDeleted(String reasonType) {
        subscription.getSpec().getReason().setReasonType(reasonType);
        var renderer = mock(NotificationTemplateRender.class);
        when(renderer.render(anyString(), any())).thenReturn(Mono.just(""));
        var helper = mock(EmailSenderHelper.class);
        var mailSender = mock(JavaMailSender.class);
        var preparator = mock(MimeMessagePreparator.class);
        when(helper.createJavaMailSender(any())).thenReturn(mailSender);
        when(helper.createMimeMessagePreparator(any(), eq("test@example.com"), anyString(), anyString(), anyString()))
                .thenReturn(preparator);
        doThrow(new MailSendException("temporary failure"))
                .doNothing()
                .when(mailSender)
                .send(preparator);
        var notifier = new EmailNotifier(new DefaultSubscriberEmailResolver(client), renderer, helper);
        var sender = spy(new DefaultNotificationSender(client, mock(ExtensionGetter.class)));
        doReturn(Mono.just(notifier)).when(sender).selectNotifier("email");
        var context = new NotificationContext();
        var message = new NotificationContext.Message();
        message.setRecipient(subscription.getSpec().getSubscriber().getName());
        var payload = new NotificationContext.MessagePayload();
        payload.setTitle("Verification");
        payload.setRawBody("Code");
        payload.setHtmlBody("<p>Code</p>");
        payload.setAttributes(new ReasonAttributes());
        message.setPayload(payload);
        context.setMessage(message);
        context.setSenderConfig(JsonNodeFactory.instance.objectNode().put("enable", true));
        try {
            sender.sendNotification("email", context).block(Duration.ofSeconds(5));
            var queue = (RequestQueue<DefaultNotificationSender.QueueItem>)
                    ReflectionTestUtils.getField(sender, "requestQueue");
            assertThat(queue).isNotNull();
            var item = queue.peek().getEntry();
            when(client.listTopNames(eq(Reason.class), any(), any(), eq(1))).thenReturn(Flux.empty());
            when(client.delete(subscription)).thenReturn(Mono.just(subscription));
            task.cleanUpSubscription("old", CUTOFF).block();
            when(client.fetch(Subscription.class, "old")).thenReturn(Mono.empty());

            assertThatThrownBy(() -> sender.reconcile(item)).isInstanceOf(MailSendException.class);
            sender.reconcile(item);

            verify(mailSender, times(2)).send(preparator);
            verify(client, times(2)).fetch(eq(Subscription.class), anyString());
            assertThat(item.getTimes()).isEqualTo(2);
        } finally {
            sender.stop();
        }
    }

    @Test
    void leavesOtherSubscriptionsAndNonCoreShapesUntouched() {
        List<Consumer<Subscription>> changes = List.of(
                sub -> sub.getSpec().getReason().setReasonType("new-device-login"),
                sub -> sub.getSpec().getReason().setReasonType("comment-reply"),
                sub -> sub.getSpec().getReason().setReasonType("order-paid"),
                sub -> sub.getSpec().getReason().setReasonType("email-verification-custom"),
                sub -> sub.getSpec().getSubscriber().setName("registered-user"),
                sub -> sub.getSpec().getReason().setExpression("true"),
                sub -> sub.getSpec().getReason().getSubject().setName(null),
                sub -> sub.getSpec().getReason().getSubject().setName("another-subscriber"),
                sub -> sub.getSpec().getReason().getSubject().setApiVersion("v1alpha1"),
                sub -> sub.getMetadata().setDeletionTimestamp(CUTOFF.minusSeconds(1)),
                sub -> sub.getMetadata().setFinalizers(Set.of("custom-finalizer")),
                sub -> sub.getMetadata().setCreationTimestamp(CUTOFF),
                sub -> sub.getMetadata().setCreationTimestamp(null));
        for (var change : changes) {
            subscription = subscription("old");
            change.accept(subscription);
            task.cleanUpSubscription("old", CUTOFF).as(StepVerifier::create).verifyComplete();
        }
        verify(client, never()).listTopNames(any(), any(), any(), anyInt());
        verify(client, never()).delete(any());
    }

    @Test
    void snapshotsNamesAndContinuesPastProtectedRecordsWithoutOffsetPagination() {
        var names = IntStream.range(0, 205).mapToObj(i -> "subscription-" + i).toList();
        when(client.listAllNames(eq(Subscription.class), any(), any())).thenReturn(Flux.fromIterable(names));
        when(client.fetch(eq(Subscription.class), anyString()))
                .thenAnswer(invocation -> Mono.just(subscription(invocation.getArgument(1))));
        when(client.listTopNames(eq(Reason.class), any(), any(), eq(1))).thenReturn(Flux.just("pending"), Flux.empty());
        when(client.delete(any(Subscription.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.withVirtualTime(task::cleanUpSubscriptions)
                .thenAwait(Duration.ofSeconds(5))
                .expectNext(204L)
                .verifyComplete();

        verify(client, times(1)).listAllNames(eq(Subscription.class), any(), any());
        verify(client, times(204)).delete(any(Subscription.class));
        verify(client, never()).listBy(any(), any(), any());
    }

    static Subscription subscription(String name) {
        var subscription = new Subscription();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setVersion(1L);
        metadata.setCreationTimestamp(CUTOFF.minus(Duration.ofDays(400)));
        subscription.setMetadata(metadata);
        var spec = new Subscription.Spec();
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail("test@example.com").name());
        spec.setSubscriber(subscriber);
        var reason = new Subscription.InterestReason();
        reason.setReasonType("email-verification");
        reason.setSubject(Subscription.ReasonSubject.builder()
                .apiVersion("User")
                .kind("User")
                .name(subscriber.getName())
                .build());
        spec.setReason(reason);
        subscription.setSpec(spec);
        return subscription;
    }
}
