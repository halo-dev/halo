package run.halo.app.notification;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Reason;

/**
 * <p>{@link NotificationSender} used to send notification.</p>
 * <p>Send notification is a time-consuming task, so we use a queue to send notification
 * asynchronously.</p>
 * <p>The caller may not be reactive, and in many cases it is blocking called
 * {@link NotificationCenter#notify(Reason)}, so here use the queue to ensure asynchronous
 * sending of notification without blocking the calling thread.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
@FunctionalInterface
public interface NotificationSender {

    Mono<Void> sendNotification(String notifierExtensionName, NotificationContext context);
}
