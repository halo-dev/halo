package run.halo.app.notification;

import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.greaterThan;
import static run.halo.app.extension.index.query.Queries.in;
import static run.halo.app.extension.index.query.Queries.isNull;
import static run.halo.app.extension.index.query.Queries.lessThan;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;

/** Reclaims core one-time email subscriptions once no pending notification needs them. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransientEmailSubscriptionCleanupTask {
    private static final Set<String> REASON_TYPES =
            Set.of("email-verification", "login-email-code", "sudo-email-code", "reset-password-by-email");

    private static final int BATCH_SIZE = 1000;

    // Retained across scheduled runs so permanently protected records cannot starve the tail.
    private volatile String nameCursor;

    private final ReactiveExtensionClient client;
    private final TransientEmailNotificationCoordinator coordinator;

    /** The cron can be set to {@code -} to disable cleanup. Publication must use the same Halo instance. */
    @Scheduled(cron = "${halo.notification.transient-email-cleanup-cron:0 30 3 * * ?}")
    public void cleanUp() {
        var deleted = cleanUpSubscriptions().block();
        log.info("Deleted {} unused transient email subscription(s)", deleted);
    }

    Mono<Long> cleanUpSubscriptions() {
        return Mono.defer(() -> {
            var cutoff = Instant.now();
            return Flux.defer(() -> cleanUpBatch(cutoff))
                    .repeat(() -> nameCursor != null)
                    .take(Duration.ofMinutes(10))
                    .count();
        });
    }

    private Flux<Subscription> cleanUpBatch(Instant cutoff) {
        var builder = ListOptions.builder()
                .andQuery(in("spec.reason.reasonType", REASON_TYPES))
                .andQuery(isNull("metadata.deletionTimestamp"))
                .andQuery(lessThan("metadata.creationTimestamp", cutoff));
        if (nameCursor != null) {
            builder.andQuery(greaterThan("metadata.name", nameCursor));
        }
        // Snapshot each batch before deleting; keyset pagination also advances past protected records.
        return client.listTopNames(Subscription.class, builder.build(), Sort.by("metadata.name"), BATCH_SIZE)
                .collectList()
                .flatMapMany(names -> {
                    if (names.isEmpty()) {
                        nameCursor = null;
                        return Flux.empty();
                    }
                    return Flux.fromIterable(names)
                            .concatMap(
                                    name -> cleanUpSubscription(name, cutoff)
                                            .delaySubscription(Duration.ofMillis(10))
                                            .doOnSuccess(ignored -> nameCursor = name),
                                    1);
                });
    }

    Mono<Subscription> cleanUpSubscription(String name, Instant cutoff) {
        return client.fetch(Subscription.class, name)
                .filter(subscription -> isCandidate(subscription, cutoff))
                .flatMap(subscription -> {
                    var subscriber = subscription.getSpec().getSubscriber().getName();
                    var reasonType = subscription.getSpec().getReason().getReasonType();
                    return coordinator.tryCleanup(
                            reasonType,
                            subscriber,
                            () -> client.fetch(Subscription.class, name)
                                    .filter(current -> isCandidate(current, cutoff))
                                    .filter(current -> subscriber.equals(
                                            current.getSpec().getSubscriber().getName()))
                                    .filter(current -> reasonType.equals(
                                            current.getSpec().getReason().getReasonType()))
                                    .filterWhen(
                                            current -> hasPendingReason(current).map(pending -> !pending))
                                    .flatMap(client::delete));
                })
                // Never retry deletion against a newer version without checking its dependencies again.
                .onErrorResume(OptimisticLockingFailureException.class, error -> Mono.empty())
                .onErrorResume(error -> {
                    log.warn("Skipping transient email subscription {} after cleanup failure", name, error);
                    return Mono.empty();
                });
    }

    private Mono<Boolean> hasPendingReason(Subscription subscription) {
        // Only retrieve this subject's Reasons. Combining global type/deletion indexes would
        // copy their entire matching sets for every candidate, even with a top-N limit.
        var options = ListOptions.builder()
                .andQuery(equal(
                        "spec.subject",
                        subscription.getSpec().getReason().getSubject().toString()))
                .build();
        return client.listAll(Reason.class, options, Sort.unsorted())
                .any(reason -> reason.getMetadata().getDeletionTimestamp() == null
                        && subscription
                                .getSpec()
                                .getReason()
                                .getReasonType()
                                .equals(reason.getSpec().getReasonType()));
    }

    private boolean isCandidate(Subscription subscription, Instant cutoff) {
        var metadata = subscription.getMetadata();
        if (metadata.getDeletionTimestamp() != null
                || metadata.getCreationTimestamp() == null
                || !metadata.getCreationTimestamp().isBefore(cutoff)
                || !CollectionUtils.isEmpty(metadata.getFinalizers())) {
            return false;
        }
        var spec = subscription.getSpec();
        if (spec == null || spec.getSubscriber() == null || spec.getReason() == null) {
            return false;
        }
        var subscriber = spec.getSubscriber().getName();
        var reason = spec.getReason();
        var subject = reason.getSubject();
        return StringUtils.isNotBlank(subscriber)
                && UserIdentity.of(subscriber)
                        .getEmail()
                        .filter(StringUtils::isNotBlank)
                        .isPresent()
                && reason.getReasonType() != null
                && REASON_TYPES.contains(reason.getReasonType())
                && StringUtils.isBlank(reason.getExpression())
                && subject != null
                && User.KIND.equals(subject.getKind())
                && new GroupVersion(User.GROUP, User.KIND).toString().equals(subject.getApiVersion())
                && subscriber.equals(subject.getName());
    }
}
