package run.halo.app.notification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Subscription;

public interface SubscriptionService {

    Flux<Subscription> list(Subscription.Subscriber subscriber,
        Subscription.InterestReason interestReason);

    Flux<Subscription> list(Subscription.Subscriber subscriber);

    Flux<Subscription> list(String reasonType);

    Mono<Subscription> remove(Subscription subscription);
}
