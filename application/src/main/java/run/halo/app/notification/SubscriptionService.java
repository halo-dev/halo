package run.halo.app.notification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ListOptions;

public interface SubscriptionService {

    /**
     * <p>List subscriptions by page one by one.only consume one page then next page will be
     * loaded.</p>
     * <p>Note that: result can not be used to delete the subscription, it is only used to query the
     * subscription.</p>
     */
    Flux<Subscription> listByPerPage(String reasonType);

    Mono<Void> remove(Subscription.Subscriber subscriber,
        Subscription.InterestReason interestReasons);

    Mono<Void> remove(Subscription.Subscriber subscriber);

    Mono<Subscription> remove(Subscription subscription);

    Flux<Subscription> removeBy(ListOptions listOptions);
}
