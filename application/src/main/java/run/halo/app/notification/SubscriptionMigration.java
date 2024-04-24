package run.halo.app.notification;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.in;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.notEqual;
import static run.halo.app.extension.index.query.QueryFactory.startsWith;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.NotificationReasonConst;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.ReactiveExtensionPaginatedIterator;

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
            new ReactiveExtensionPaginatedIterator<>(client, User.class, listOptions);
        iterator.list()
            .map(user -> user.getMetadata().getName())
            .flatMap(this::removeInternalSubscriptionForUser)
            .then()
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
                "props.repliedOwner == '%s'".formatted(anonymousSubscriber));
        }
        log.debug("Collating anonymous subscription completed.");
    }

    private Mono<Void> deleteAnonymousSubscription(Consumer<Subscription> consumer) {
        return listAnonymousSubscription()
            .flatMap(page -> Flux.fromIterable(page.getItems())
                .flatMap(subscriptionService::remove)
                .doOnNext(consumer)
                .doOnNext(subscription -> log.debug("Deleted anonymous subscription: {}",
                    subscription.getMetadata().getName())
                )
                .then(Mono.defer(
                    () -> page.hasNext() ? deleteAnonymousSubscription(consumer) : Mono.empty()))
            );
    }

    Mono<ListResult<Subscription>> listAnonymousSubscription() {
        var reason = new Subscription.InterestReason();
        Subscription.InterestReason.ensureSubjectHasValue(reason);
        var listOptions = new ListOptions();
        var query = and(startsWith("spec.subscriber", AnonymousUserConst.PRINCIPAL),
            notEqual("spec.reason.subject", reason.getSubject().toString()),
            in("spec.reason.reasonType", Set.of(NotificationReasonConst.NEW_COMMENT_ON_POST,
                NotificationReasonConst.NEW_COMMENT_ON_PAGE,
                NotificationReasonConst.SOMEONE_REPLIED_TO_YOU))
        );
        listOptions.setFieldSelector(FieldSelector.of(query));
        // forever loop first page until no more to delete
        var pageRequest = PageRequestImpl.of(1, 200,
            Sort.by("metadata.creationTimestamp", "metadata.name"));
        return client.listBy(Subscription.class, listOptions, pageRequest);
    }

    void createSubscription(String name, String expression) {
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(NotificationReasonConst.SOMEONE_REPLIED_TO_YOU);
        interestReason.setExpression(expression);
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(name);
        notificationCenter.subscribe(subscriber, interestReason).block();
    }

    private Mono<Void> removeInternalSubscriptionForUser(String username) {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(username);

        var commentOnPost =
            createInterestReason(NotificationReasonConst.NEW_COMMENT_ON_POST, Post.class);
        var unsubscribeCommentOnPost = notificationCenter.unsubscribe(subscriber, commentOnPost);

        var commentOnPage =
            createInterestReason(NotificationReasonConst.NEW_COMMENT_ON_PAGE, SinglePage.class);
        var unsubscribeCommentOnPage = notificationCenter.unsubscribe(subscriber, commentOnPage);

        var replyOnComment =
            createInterestReason(NotificationReasonConst.SOMEONE_REPLIED_TO_YOU, Comment.class);
        var unsubscribeReplyOnComment = notificationCenter.unsubscribe(subscriber, replyOnComment);

        var replyOnReply =
            createInterestReason(NotificationReasonConst.SOMEONE_REPLIED_TO_YOU, Reply.class);
        var unsubscribeReplyOnReply = notificationCenter.unsubscribe(subscriber, replyOnReply);

        return Mono.when(unsubscribeCommentOnPost, unsubscribeCommentOnPage,
                unsubscribeReplyOnComment,
                unsubscribeReplyOnReply)
            .doOnSuccess(unused ->
                log.debug("Collating internal subscription for user: {} completed", username));
    }

    <E extends Extension> Subscription.InterestReason createInterestReason(String type,
        Class<E> extensionClass) {
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(type);

        var reasonSubject = new Subscription.ReasonSubject();
        var gvk = GroupVersionKind.fromExtension(extensionClass);
        reasonSubject.setKind(gvk.kind());
        reasonSubject.setApiVersion(gvk.groupVersion().toString());
        interestReason.setSubject(reasonSubject);
        return interestReason;
    }
}
