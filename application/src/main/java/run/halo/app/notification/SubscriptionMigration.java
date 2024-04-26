package run.halo.app.notification;

import static run.halo.app.content.NotificationReasonConst.NEW_COMMENT_ON_PAGE;
import static run.halo.app.content.NotificationReasonConst.NEW_COMMENT_ON_POST;
import static run.halo.app.content.NotificationReasonConst.SOMEONE_REPLIED_TO_YOU;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.in;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.startsWith;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;
import run.halo.app.infra.ReactiveExtensionPaginatedOperatorImpl;

/**
 * Subscription migration to adapt to the new expression subscribe mechanism.
 *
 * @author guqing
 * @since 2.15.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionMigration implements ApplicationListener<ApplicationStartedEvent> {
    private final NotificationCenter notificationCenter;
    private final ReactiveExtensionClient client;
    private final SubscriptionService subscriptionService;
    private final ReactiveExtensionPaginatedOperator paginatedOperator;

    @Override
    @Async
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        handleAnonymousSubscription();
        cleanupUserSubscription();
    }

    private void cleanupUserSubscription() {
        var listOptions = new ListOptions();
        var query = isNull("metadata.deletionTimestamp");
        listOptions.setFieldSelector(FieldSelector.of(query));
        var iterator =
            new ReactiveExtensionPaginatedOperatorImpl(client);
        iterator.list(User.class, listOptions)
            .map(user -> user.getMetadata().getName())
            .flatMap(this::removeInternalSubscriptionForUser)
            .then()
            .doOnSuccess(unused -> log.info("Cleanup user subscription completed"))
            .block();
    }

    private void handleAnonymousSubscription() {
        log.debug("Start to collating anonymous subscription...");
        Set<String> anonymousSubscribers = new HashSet<>();
        deleteAnonymousSubscription(subscription -> {
            var name = subscription.getSpec().getSubscriber().getName();
            anonymousSubscribers.add(name);
        }).block();
        if (anonymousSubscribers.isEmpty()) {
            return;
        }

        // anonymous only subscribe some-one-replied-to-you reason
        for (String anonymousSubscriber : anonymousSubscribers) {
            createSubscription(anonymousSubscriber,
                SOMEONE_REPLIED_TO_YOU,
                "props.repliedOwner == '%s'".formatted(anonymousSubscriber)).block();
        }
        log.info("Collating anonymous subscription completed.");
    }

    private Mono<Void> deleteAnonymousSubscription(Consumer<Subscription> consumer) {
        var listOptions = new ListOptions();
        var query = and(startsWith("spec.subscriber", AnonymousUserConst.PRINCIPAL),
            isNull("spec.reason.expression"),
            isNull("metadata.deletionTimestamp"),
            in("spec.reason.reasonType", Set.of(NEW_COMMENT_ON_POST,
                NEW_COMMENT_ON_PAGE,
                SOMEONE_REPLIED_TO_YOU))
        );
        listOptions.setFieldSelector(FieldSelector.of(query));
        return paginatedOperator.deleteInitialBatch(Subscription.class, listOptions)
            .doOnNext(consumer)
            .doOnNext(subscription -> log.debug("Deleted anonymous subscription: {}",
                subscription.getMetadata().getName())
            )
            .then();
    }

    private Mono<Void> removeInternalSubscriptionForUser(String username) {
        log.debug("Start to collating internal subscription for user: {}", username);
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(username);

        var listOptions = new ListOptions();
        var fieldQuery = and(isNull("metadata.deletionTimestamp"),
            equal("spec.subscriber", subscriber.toString()),
            in("spec.reason.reasonType", Set.of(
                NEW_COMMENT_ON_POST,
                NEW_COMMENT_ON_PAGE,
                SOMEONE_REPLIED_TO_YOU
            ))
        );
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));

        return subscriptionService.removeBy(listOptions)
            .map(subscription -> {
                var name = subscription.getSpec().getSubscriber().getName();
                var reason = subscription.getSpec().getReason();
                String expression = switch (reason.getReasonType()) {
                    case NEW_COMMENT_ON_POST -> "props.postOwner == '%s'".formatted(name);
                    case NEW_COMMENT_ON_PAGE -> "props.pageOwner == '%s'".formatted(name);
                    case SOMEONE_REPLIED_TO_YOU -> "props.repliedOwner == '%s'".formatted(name);
                    // never happen
                    default -> null;
                };
                return new SubscriptionSummary(name, reason.getReasonType(), expression);
            })
            .distinct()
            .flatMap(summary -> createSubscription(summary.subscriber(), summary.reasonType(),
                summary.expression()))
            .then()
            .doOnSuccess(unused ->
                log.debug("Collating internal subscription for user: {} completed", username));
    }

    Mono<Void> createSubscription(String name, String reasonType, String expression) {
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(reasonType);
        interestReason.setExpression(expression);
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(name);
        log.debug("Create subscription for user: {} with reasonType: {}", name, reasonType);
        return notificationCenter.subscribe(subscriber, interestReason).then();
    }

    record SubscriptionSummary(String subscriber, String reasonType, String expression) {
    }
}
