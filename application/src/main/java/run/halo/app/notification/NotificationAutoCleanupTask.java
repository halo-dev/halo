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
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;
import run.halo.app.infra.utils.ReactiveUtils;

/**
 * Periodically cleans up old {@link Notification} records to prevent unbounded data growth.
 *
 * <p>Read notifications older than the configured retention period are deleted.</p>
 *
 * <p>The cleanup is skipped entirely when
 * {@link NotificationProperties#isEnabled()} is {@code false}.</p>
 *
 * @author programmerloverun
 * @see RememberTokenCleaner the pattern this class follows
 * @since 2.25.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAutoCleanupTask {

    static final Duration BLOCKING_TIMEOUT = ReactiveUtils.DEFAULT_TIMEOUT;

    private final ReactiveExtensionPaginatedOperator paginatedOperator;
    private final NotificationProperties properties;

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

        long deleted = cleanUpByRetentionDays();
        log.info("Deleted {} notification(s) older than {} day(s).",
            deleted, properties.getRetentionDays());

        log.info("Notification auto-cleanup finished. Total deleted: {}.", deleted);
    }

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
     * Builds a {@link ListOptions} that matches notifications which:
     * <ul>
     *   <li>are not already being deleted ({@code metadata.deletionTimestamp} is null)</li>
     *   <li>were created before {@code threshold}</li>
     *   <li>have already been read ({@code spec.unread} is {@code false})</li>
     * </ul>
     */
    ListOptions buildExpiredListOptions(Instant threshold) {
        Query query = and(
            isNull("metadata.deletionTimestamp"),
            lessThan("metadata.creationTimestamp", threshold),
            equal("spec.unread", false)
        );
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(query));
        return listOptions;
    }

    /**
     * Returns the cut-off instant: notifications created before this point are expired.
     */
    Instant getRetentionThreshold() {
        return Instant.now().minus(properties.getRetentionDays(), ChronoUnit.DAYS);
    }
}
