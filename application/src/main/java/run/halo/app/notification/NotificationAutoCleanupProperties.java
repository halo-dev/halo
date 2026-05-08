package run.halo.app.notification;

import lombok.Data;

/**
 * Configuration properties for automatic notification cleanup.
 *
 * <p>Notifications older than {@code retentionDays} days will be deleted
 * when {@code retentionDays} is greater than 0.</p>
 *
 * <p>Example configuration in application.yaml:</p>
 * <pre>
 * halo:
 *   notification:
 *     auto-cleanup:
 *       enabled: true
 *       retention-days: 30
 *       cleanup-cron: "0 0 3 * * ?"
 * </pre>
 *
 * @author programmerloverun
 * @since 2.22.0
 */
@Data
public class NotificationAutoCleanupProperties {

    /**
     * Whether to enable automatic notification cleanup. Default is {@code false}.
     */
    private boolean enabled = false;

    /**
     * Number of days to retain notifications.
     * Notifications older than this value will be deleted.
     * A value of 0 or negative disables this strategy.
     * Default is 30 days.
     */
    private int retentionDays = 30;

    /**
     * Cron expression for the cleanup schedule.
     * Default is every day at 3:00 AM: {@code "0 0 3 * * ?"}.
     */
    private String cleanupCron = "0 0 3 * * ?";

    /**
     * Returns true if the retention-by-days strategy is active.
     */
    public boolean isRetentionByDaysEnabled() {
        return enabled && retentionDays > 0;
    }
}
