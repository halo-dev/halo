package run.halo.app.notification;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;

/**
 * Notification center to notify and manage notifications.
 *
 * @author guqing
 * @since 2.10.0
 */
public interface NotificationCenter {

    /**
     * Notifies the subscriber with the given reason.
     *
     * @param reason reason to notify
     */
    Mono<Void> notify(Reason reason);

    /**
     * Subscribes to the given subject with the given reason.
     *
     * @param subscriber subscriber to subscribe to
     * @param reason interest reason to subscribe
     * @return a subscription
     */
    Mono<Subscription> subscribe(Subscription.Subscriber subscriber,
        Subscription.InterestReason reason);

    /**
     * Unsubscribes by the given subject.
     *
     * @param subscriber subscriber to unsubscribe
     */
    Mono<Void> unsubscribe(Subscription.Subscriber subscriber);

    /**
     * Unsubscribes by the given subject and reason.
     *
     * @param subscriber subscriber to unsubscribe
     * @param reason reason to unsubscribe
     */
    Mono<Void> unsubscribe(Subscription.Subscriber subscriber, Subscription.InterestReason reason);
}
