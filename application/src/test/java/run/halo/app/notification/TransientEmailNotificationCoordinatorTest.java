package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Subscription;

class TransientEmailNotificationCoordinatorTest {
    private final TransientEmailNotificationCoordinator coordinator = new TransientEmailNotificationCoordinator();

    @Test
    void skipsCleanupUntilPublicationCompletesEvenAfterCallerCancels() {
        var pending = Sinks.<Void>one();
        var cleanup = mock(Runnable.class);
        var publication = coordinator
                .publish("email-verification", "subscriber", pending::asMono)
                .subscribe();
        publication.dispose();

        coordinator
                .tryCleanup(
                        "email-verification",
                        "subscriber",
                        () -> Mono.fromRunnable(cleanup).then(Mono.empty()))
                .as(StepVerifier::create)
                .verifyComplete();
        verifyNoInteractions(cleanup);

        pending.tryEmitEmpty();
        coordinator
                .tryCleanup(
                        "email-verification",
                        "subscriber",
                        () -> Mono.fromRunnable(cleanup).then(Mono.empty()))
                .block();
        verify(cleanup).run();
    }

    @Test
    void serializesPublicationWithCleanupButDoesNotBlockOtherSubscribers() {
        var deletion = Sinks.<Subscription>one();
        coordinator
                .tryCleanup("email-verification", "subscriber", deletion::asMono)
                .subscribe();
        var calls = new ArrayList<String>();

        coordinator
                .publish("email-verification", "subscriber", () -> Mono.fromRunnable(() -> calls.add("first")))
                .subscribe();
        coordinator
                .publish("email-verification", "subscriber", () -> Mono.fromRunnable(() -> calls.add("second")))
                .subscribe();
        coordinator
                .publish("email-verification", "other", () -> Mono.fromRunnable(() -> calls.add("other")))
                .block();
        assertThat(calls).containsExactly("other");

        deletion.tryEmitEmpty();
        assertThat(calls).containsExactly("other", "first", "second");
    }

    @Test
    void releasesGuardAfterFailureIncludingSynchronousSupplierFailure() {
        for (var synchronous : List.of(false, true)) {
            coordinator
                    .publish("email-verification", "subscriber", () -> {
                        if (synchronous) {
                            throw new IllegalStateException("publication failed");
                        }
                        return Mono.error(new IllegalStateException("publication failed"));
                    })
                    .as(StepVerifier::create)
                    .expectErrorMessage("publication failed")
                    .verify();
            var subscription = new Subscription();
            coordinator
                    .tryCleanup("email-verification", "subscriber", () -> Mono.just(subscription))
                    .as(StepVerifier::create)
                    .expectNext(subscription)
                    .verifyComplete();
        }
    }

    @Test
    void retryStartsANewPublicationInsteadOfReplayingCachedFailure() {
        var attempts = new AtomicInteger();
        coordinator
                .publish(
                        "email-verification",
                        "subscriber",
                        () -> attempts.incrementAndGet() == 1
                                ? Mono.error(new IllegalStateException("retry"))
                                : Mono.empty())
                .retry(1)
                .as(StepVerifier::create)
                .verifyComplete();
        assertThat(attempts).hasValue(2);
    }

    @Test
    void separatesDifferentReasonTypesForTheSameSubscriber() {
        var pending = Sinks.<Void>one();
        coordinator.publish("email-verification", "subscriber", pending::asMono).subscribe();
        var subscription = new Subscription();
        coordinator
                .tryCleanup("login-email-code", "subscriber", () -> Mono.just(subscription))
                .as(StepVerifier::create)
                .expectNext(subscription)
                .verifyComplete();
        coordinator
                .publish("sudo-email-code", "subscriber", Mono::empty)
                .as(StepVerifier::create)
                .verifyComplete();
        coordinator
                .tryCleanup("email-verification", "subscriber", () -> Mono.just(subscription))
                .as(StepVerifier::create)
                .verifyComplete();
        pending.tryEmitEmpty();
    }
}
