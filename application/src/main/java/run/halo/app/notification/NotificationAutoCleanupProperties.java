package run.halo.app.notification;

import lombok.Data;

/**
 * Configuration properties for automatic notification cleanup.
 *
 * <p>Supports two cleanup strategies:</p>
 * <ul>
 *   <li><b>Retention by days</b>: removes notifications older than {@code retentionDays} days
 *       (when {@code retentionDays} &gt; 0)</li>
 *   <li><b>Retention by count</b>: keeps at most {@code maxCountPerUser} notifications per user,
 *       deleting the oldest ones when the limit is exceeded
 *       (when {@code maxCountPerUser} &gt; 0)</li>
 * </ul>
 *
 * <p>Both strategies can be enabled simultaneously.</p>
 *
 * <p>Example configuration in application.yaml:</p>
 * <pre>
 * halo:
 *   notification:
 *     auto-cleanup:
 *       enabled: true
 *       retention-days: 30
 *       max-count-per-user: 200
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
     * Maximum number of notifications to retain per user.
     * When the number of notifications for a user exceeds this limit,
     * the oldest notifications will be deleted.
     * A value of 0 or negative disables this strategy.
     * Default is 200.
     */
    private int maxCountPerUser = 200;

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

    /**
     * Returns true if the retention-by-count strategy is active.
     */
    public boolean isRetentionByCountEnabled() {
        return enabled && maxCountPerUser > 0;
    }
}
