package run.halo.app.notification;

import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.isNull;
import static run.halo.app.extension.index.query.Queries.lessThan;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;
import run.halo.app.infra.utils.ReactiveUtils;

/**
 * Periodically cleans up old {@link Notification} records to prevent unbounded data growth.
 *
 * <p>Two optional cleanup strategies (can be used independently or together):</p>
 * <ol>
 *   <li><b>Retention by days</b>: deletes notifications older than N days.</li>
 *   <li><b>Retention by count per user</b>: keeps only the newest M notifications per user,
 *       deleting the oldest ones that exceed the limit.</li>
 * </ol>
 *
 * <p>The cleanup is skipped entirely when
 * {@link NotificationAutoCleanupProperties#isEnabled()} is {@code false}.</p>
 *
 * @author programmerloverun
 * @see RememberTokenCleaner the pattern this class follows
 * @since 2.22.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAutoCleanupTask {

    static final Duration BLOCKING_TIMEOUT = ReactiveUtils.DEFAULT_TIMEOUT;

    private final ReactiveExtensionPaginatedOperator paginatedOperator;
    private final ReactiveExtensionClient client;
    private final NotificationAutoCleanupProperties properties;

    /**
     * Triggered by the cron expression configured via
     * {@code halo.notification.auto-cleanup.cleanup-cron} (defaults to 3:00 AM daily).
     */
    @Scheduled(cron = "${halo.notification.auto-cleanup.cleanup-cron:0 0 3 * * ?}")
    public void cleanUp() {
        if (!properties.isEnabled()) {
            log.debug("Notification auto-cleanup is disabled, skipping.");
            return;
        }
        log.info("Starting notification auto-cleanup job...");
        long totalDeleted = 0;

        // Strategy 1: delete by retention days
        if (properties.isRetentionByDaysEnabled()) {
            long deleted = cleanUpByRetentionDays();
            totalDeleted += deleted;
            log.info("Retention-by-days: deleted {} notification(s) older than {} day(s).",
                deleted, properties.getRetentionDays());
        }

        // Strategy 2: delete by max count per user
        if (properties.isRetentionByCountEnabled()) {
            long deleted = cleanUpByMaxCountPerUser();
            totalDeleted += deleted;
            log.info("Retention-by-count: deleted {} notification(s) exceeding {} per-user limit.",
                deleted, properties.getMaxCountPerUser());
        }

        log.info("Notification auto-cleanup finished. Total deleted: {}.", totalDeleted);
    }

    // -----------------------------------------------------------------------
    // Strategy 1: retention by days
    // -----------------------------------------------------------------------

    long cleanUpByRetentionDays() {
        var listOptions = buildExpiredListOptions(getRetentionThreshold());
        var count = new AtomicLong(0);
        paginatedOperator.deleteInitialBatch(Notification.class, listOptions)
            .doOnNext(n -> count.incrementAndGet())
            .then()
            .block(BLOCKING_TIMEOUT);
        return count.get();
    }

    /**
     * Builds a {@link ListOptions} that matches notifications whose
     * {@code metadata.creationTimestamp} is older than {@code threshold} and are not already
     * being deleted ({@code metadata.deletionTimestamp} is null).
     */
    ListOptions buildExpiredListOptions(Instant threshold) {
        Query query = and(
            isNull("metadata.deletionTimestamp"),
            lessThan("metadata.creationTimestamp", threshold.toString())
        );
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(query));
        return listOptions;
    }

    /**
     * Returns the cut-off instant: notifications created before this point are expired
     * under the retention-by-days strategy.
     */
    Instant getRetentionThreshold() {
        return Instant.now().minus(properties.getRetentionDays(), ChronoUnit.DAYS);
    }

    // -----------------------------------------------------------------------
    // Strategy 2: retention by count per user
    // -----------------------------------------------------------------------

    long cleanUpByMaxCountPerUser() {
        var totalDeleted = new AtomicLong(0);
        client.listAll(User.class, new ListOptions(), null)
            .map(user -> user.getMetadata().getName())
            .flatMap(username -> cleanUpUserNotifications(username, totalDeleted))
            .then()
            .block(BLOCKING_TIMEOUT);
        return totalDeleted.get();
    }

    private Mono<Void> cleanUpUserNotifications(String username, AtomicLong totalDeleted) {
        int maxCount = properties.getMaxCountPerUser();
        return countUserNotifications(username)
            .flatMap(count -> {
                long excess = count - maxCount;
                if (excess <= 0) {
                    return Mono.empty();
                }
                log.debug("User '{}' has {} notifications, {} exceed the limit of {}.",
                    username, count, excess, maxCount);
                return deleteOldestUserNotifications(username, (int) excess)
                    .doOnNext(n -> totalDeleted.incrementAndGet())
                    .then();
            });
    }

    private Mono<Long> countUserNotifications(String username) {
        return client.listBy(Notification.class,
                buildUserNotificationListOptions(username),
                PageRequestImpl.ofSize(1))
            .map(result -> result.getTotal());
    }

    private Flux<Notification> deleteOldestUserNotifications(String username, int limit) {
        return paginatedOperator.list(Notification.class,
                buildUserNotificationListOptions(username))
            .take(limit)
            .flatMap(client::delete);
    }

    ListOptions buildUserNotificationListOptions(String username) {
        Query query = and(
            isNull("metadata.deletionTimestamp"),
            equal("spec.recipient", username)
        );
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(query));
        return listOptions;
    }
}
