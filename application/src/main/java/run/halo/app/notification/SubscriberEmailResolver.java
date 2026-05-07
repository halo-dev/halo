package run.halo.app.notification;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Subscription;

/**
 * {@link SubscriberEmailResolver} used to resolve email from {@link Subscription.Subscriber} .
 *
 * @author guqing
 * @since 2.10.0
 */
public interface SubscriberEmailResolver {

    Mono<String> resolve(Subscription.Subscriber subscriber);

    /**
     * Creates an email subscriber from email.
     *
     * @param email email
     * @return email subscriber
     */
    Subscription.Subscriber ofEmail(String email);
}
