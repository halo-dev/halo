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
     * Mark the specified notification as read.
     *
     * @param name notification name
     * @return read notification
     */
    Mono<Notification> markAsRead(String username, String name);

    /**
     * Mark the specified notifications as read.
     *
     * @param names the names of notifications
     * @return the names of read notification that has been marked as read
     */
    Flux<String> markSpecifiedAsRead(String username, List<String> names);

    Mono<Notification> deleteByName(String username, String name);
}
