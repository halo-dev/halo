package run.halo.app.notification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ListOptions;

public interface SubscriptionService {

    /**
     * List subscriptions by page one by one.only consume one page then next page will be loaded.
     *
     * <p>Note that: result can not be used to delete the subscription, it is only used to query the subscription.
     */
    Flux<Subscription> listByPerPage(String reasonType);

    /**
     * List subscriptions by reasonType with subject-based filtering to avoid pulling all subscriptions for a reason
     * type into memory.
     *
     * <p>This method constructs two targeted queries:
     *
     * <ol>
     *   <li>Subscriptions whose subject matches the given subject (exact name or wildcard)
     *   <li>Subscriptions that use expression-based matching (isNotNull on spec.reason.expression)
     * </ol>
     *
     * @param reasonType the reason type to match
     * @param reasonSubject the reason's subject used to build the subject filter
     * @return a paginated flux of matching subscriptions
     * @since 2.25.3
     */
    Flux<Subscription> listByPerPage(String reasonType, Reason.Subject reasonSubject);

    Mono<Void> remove(Subscription.Subscriber subscriber, Subscription.InterestReason interestReasons);

    Mono<Void> remove(Subscription.Subscriber subscriber);

    Mono<Subscription> remove(Subscription subscription);

    Flux<Subscription> removeBy(ListOptions listOptions);
}
