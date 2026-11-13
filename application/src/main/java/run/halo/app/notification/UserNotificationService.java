package run.halo.app.notification;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListResult;

/**
 * Notification service.
 *
 * @author guqing
 * @since 2.10.0
 */
public interface UserNotificationService {

    /**
     * List notifications for the authenticated user.
     *
     * @param query query object
     * @return a page result of notifications
     */
    Mono<ListResult<Notification>> listByUser(String username, UserNotificationQuery query);

    /**
     * Get the specified notification.
     *
     * @param name notification name
     * @return the notification if it belongs to the user
     */
    Mono<Notification> getByName(String username, String name);

    /**
     * Mark the specified notification as read.
     *
     * @param name notification name
     * @return read notification
     */
    Mono<Notification> markAsRead(String username, String name);

    /**
     * Mark all notifications of the user as read.
     *
     * @return the names of notifications that have been marked as read
     */
    Flux<String> markAllAsRead(String username);

    /**
     * Mark the specified notifications as read.
     *
     * @param names the names of notifications
     * @return the names of read notification that has been marked as read
     */
    Flux<String> markSpecifiedAsRead(String username, List<String> names);

    Mono<Notification> deleteByName(String username, String name);

    /**
     * Delete all notifications matching the query.
     *
     * @param query query object containing the recipient and filters
     */
    Mono<Void> deleteAllByUser(UserNotificationQuery query);
}
