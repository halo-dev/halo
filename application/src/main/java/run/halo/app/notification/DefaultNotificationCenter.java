package run.halo.app.notification;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.or;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.core.extension.notification.NotifierDescriptor;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.notification.endpoint.SubscriptionRouter;

/**
 * A default implementation of {@link NotificationCenter}.
 *
 * @author guqing
 * @since 2.10.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultNotificationCenter implements NotificationCenter {
    private final ReactiveExtensionClient client;
    private final NotificationSender notificationSender;
    private final NotifierConfigStore notifierConfigStore;
    private final ReasonNotificationTemplateSelector notificationTemplateSelector;
    private final UserNotificationPreferenceService userNotificationPreferenceService;
    private final NotificationTemplateRender notificationTemplateRender;
    private final SubscriptionRouter subscriptionRouter;

    @Override
    public Mono<Void> notify(Reason reason) {
        var reasonSubject = reason.getSpec().getSubject();
        var subscriptionReasonSubject = Subscription.ReasonSubject.builder()
            .apiVersion(reasonSubject.getApiVersion())
            .kind(reasonSubject.getKind())
            .name(reasonSubject.getName())
            .build();
        return listObservers(reason.getSpec().getReasonType(), subscriptionReasonSubject)
            .doOnNext(subscription -> {
                log.debug("Dispatching notification to subscriber [{}] for reason [{}]",
                    subscription.getSpec().getSubscriber(), reason.getMetadata().getName());
            })
            .publishOn(Schedulers.boundedElastic())
            .flatMap(subscription -> dispatchNotification(reason, subscription))
            .then();
    }

    @Override
    public Mono<Subscription> subscribe(Subscription.Subscriber subscriber,
        Subscription.InterestReason reason) {
        return listSubscription(subscriber, reason)
            .next()
            .switchIfEmpty(Mono.defer(() -> {
                var subscription = new Subscription();
                subscription.setMetadata(new Metadata());
                subscription.getMetadata().setGenerateName("subscription-");
                subscription.setSpec(new Subscription.Spec());
                subscription.getSpec().setUnsubscribeToken(Subscription.generateUnsubscribeToken());
                subscription.getSpec().setSubscriber(subscriber);
                subscription.getSpec().setReason(reason);
                return client.create(subscription);
            }));
    }

    @Override
    public Mono<Void> unsubscribe(Subscription.Subscriber subscriber) {
        // pagination query all subscriptions of the subscriber to avoid large data
        var pageRequest = PageRequestImpl.of(1, 200,
            Sort.by("metadata.creationTimestamp", "metadata.name"));
        return Flux.defer(() -> pageSubscriptionBy(subscriber, pageRequest))
            .expand(page -> page.hasNext()
                ? pageSubscriptionBy(subscriber, pageRequest)
                : Mono.empty()
            )
            .flatMap(page -> Flux.fromIterable(page.getItems()))
            .flatMap(client::delete)
            .then();
    }

    @Override
    public Mono<Void> unsubscribe(Subscription.Subscriber subscriber,
        Subscription.InterestReason reason) {
        return listSubscription(subscriber, reason)
            .flatMap(client::delete)
            .then();
    }

    Mono<ListResult<Subscription>> pageSubscriptionBy(Subscription.Subscriber subscriber,
        PageRequest pageRequest) {
        var listOptions = new ListOptions();
        var fieldQuery = equal("spec.subscriber", subscriber.getName());
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return client.listBy(Subscription.class, listOptions, pageRequest);
    }

    Flux<Subscription> listSubscription(Subscription.Subscriber subscriber,
        Subscription.InterestReason reason) {
        var listOptions = new ListOptions();
        var fieldQuery = and(
            getSubscriptionFieldQuery(reason.getReasonType(), reason.getSubject()),
            equal("spec.subscriber", subscriber.getName())
        );
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return client.listAll(Subscription.class, listOptions, defaultSort());
    }

    Flux<String> getNotifiersBySubscriber(Subscription.Subscriber subscriber, Reason reason) {
        var reasonType = reason.getSpec().getReasonType();
        return userNotificationPreferenceService.getByUser(subscriber.getName())
            .map(UserNotificationPreference::getReasonTypeNotifier)
            .map(reasonTypeNotification -> reasonTypeNotification.getNotifiers(reasonType))
            .flatMapMany(Flux::fromIterable);
    }

    Mono<Void> dispatchNotification(Reason reason, Subscription subscription) {
        var subscriber = subscription.getSpec().getSubscriber();
        return getNotifiersBySubscriber(subscriber, reason)
            .flatMap(notifierName -> client.fetch(NotifierDescriptor.class, notifierName))
            .flatMap(descriptor -> prepareNotificationElement(subscription, reason, descriptor))
            .flatMap(element -> {
                var dispatchMono = sendNotification(element);
                var innerNofificationMono = createNotification(element);
                return Mono.when(dispatchMono, innerNofificationMono);
            })
            .then();
    }

    Mono<NotificationElement> prepareNotificationElement(Subscription subscription, Reason reason,
        NotifierDescriptor descriptor) {
        return getLocaleFromSubscriber(subscription)
            .flatMap(locale -> inferenceTemplate(reason, subscription, locale))
            .map(notificationContent -> NotificationElement.builder()
                .descriptor(descriptor)
                .reason(reason)
                .subscription(subscription)
                .reasonType(notificationContent.reasonType())
                .notificationTitle(notificationContent.title())
                .reasonAttributes(notificationContent.reasonAttributes())
                .notificationRawBody(defaultString(notificationContent.rawBody()))
                .notificationHtmlBody(defaultString(notificationContent.htmlBody()))
                .build()
            );
    }

    Mono<Void> sendNotification(NotificationElement notificationElement) {
        var descriptor = notificationElement.descriptor();
        var subscription = notificationElement.subscription();
        final var notifierExtName = descriptor.getSpec().getNotifierExtName();
        return notificationContextFrom(notificationElement)
            .flatMap(notificationContext -> notificationSender.sendNotification(notifierExtName,
                    notificationContext)
                .onErrorResume(throwable -> {
                    log.error(
                        "Failed to send notification to subscriber [{}] through notifier [{}]",
                        subscription.getSpec().getSubscriber(),
                        descriptor.getSpec().getDisplayName(),
                        throwable);
                    return Mono.empty();
                })
            )
            .then();
    }

    Mono<Notification> createNotification(NotificationElement notificationElement) {
        var reason = notificationElement.reason();
        var subscription = notificationElement.subscription();
        var subscriber = subscription.getSpec().getSubscriber();
        return client.fetch(User.class, subscriber.getName())
            .flatMap(user -> {
                Notification notification = new Notification();
                notification.setMetadata(new Metadata());
                notification.getMetadata().setGenerateName("notification-");
                notification.setSpec(new Notification.NotificationSpec());
                notification.getSpec().setTitle(notificationElement.notificationTitle());
                notification.getSpec().setRawContent(notificationElement.notificationRawBody());
                notification.getSpec().setHtmlContent(notificationElement.notificationHtmlBody);
                notification.getSpec().setRecipient(subscriber.getName());
                notification.getSpec().setReason(reason.getMetadata().getName());
                notification.getSpec().setUnread(true);
                return client.create(notification);
            });
    }

    private ReasonAttributes toReasonAttributes(Reason reason) {
        var model = new ReasonAttributes();
        var attributes = reason.getSpec().getAttributes();
        if (attributes != null) {
            model.putAll(attributes);
        }
        return model;
    }

    Mono<NotificationContext> notificationContextFrom(NotificationElement element) {
        final var descriptorName = element.descriptor().getMetadata().getName();
        final var reason = element.reason();
        final var descriptor = element.descriptor();
        final var subscription = element.subscription();

        var messagePayload = new NotificationContext.MessagePayload();
        messagePayload.setTitle(element.notificationTitle());
        messagePayload.setRawBody(element.notificationRawBody());
        messagePayload.setHtmlBody(element.notificationHtmlBody());
        messagePayload.setAttributes(element.reasonAttributes());

        var message = new NotificationContext.Message();
        message.setRecipient(subscription.getSpec().getSubscriber().getName());
        message.setPayload(messagePayload);
        message.setTimestamp(reason.getMetadata().getCreationTimestamp());
        var reasonSubject = reason.getSpec().getSubject();
        var subject = NotificationContext.Subject.builder()
            .apiVersion(reasonSubject.getApiVersion())
            .kind(reasonSubject.getKind())
            .title(reasonSubject.getTitle())
            .url(reasonSubject.getUrl())
            .build();
        message.setSubject(subject);

        var notificationContext = new NotificationContext();
        notificationContext.setMessage(message);

        return Mono.just(notificationContext)
            .flatMap(context -> {
                Mono<Void> receiverConfigMono =
                    Optional.ofNullable(descriptor.getSpec().getReceiverSettingRef())
                        .map(ref -> notifierConfigStore.fetchReceiverConfig(descriptorName)
                            .doOnNext(context::setReceiverConfig)
                            .then()
                        )
                        .orElse(Mono.empty());

                Mono<Void> senderConfigMono =
                    Optional.ofNullable(descriptor.getSpec().getSenderSettingRef())
                        .map(ref -> notifierConfigStore.fetchSenderConfig(descriptorName)
                            .doOnNext(context::setSenderConfig)
                            .then()
                        )
                        .orElse(Mono.empty());

                return Mono.when(receiverConfigMono, senderConfigMono)
                    .thenReturn(context);
            });
    }

    Mono<NotificationContent> inferenceTemplate(Reason reason, Subscription subscription,
        Locale locale) {
        var reasonTypeName = reason.getSpec().getReasonType();
        var subscriber = subscription.getSpec().getSubscriber();
        return getReasonType(reasonTypeName)
            .flatMap(reasonType -> notificationTemplateSelector.select(reasonTypeName, locale)
                .flatMap(template -> {
                    final var templateContent = template.getSpec().getTemplate();
                    var model = toReasonAttributes(reason);
                    var identity = UserIdentity.of(subscriber.getName());
                    var subscriberInfo = new HashMap<>();
                    if (identity.isAnonymous()) {
                        subscriberInfo.put("displayName", identity.getEmail().orElse(""));
                    } else {
                        subscriberInfo.put("displayName", "@" + identity.name());
                    }
                    subscriberInfo.put("id", subscriber.getName());
                    model.put("subscriber", subscriberInfo);
                    model.put("unsubscribeUrl", getUnsubscribeUrl(subscription));

                    var builder = NotificationContent.builder()
                        .reasonType(reasonType)
                        .reasonAttributes(model);

                    var titleMono = notificationTemplateRender
                        .render(templateContent.getTitle(), model)
                        .doOnNext(builder::title);

                    var rawBodyMono = notificationTemplateRender
                        .render(templateContent.getRawBody(), model)
                        .doOnNext(builder::rawBody);

                    var htmlBodyMono = notificationTemplateRender
                        .render(templateContent.getHtmlBody(), model)
                        .doOnNext(builder::htmlBody);
                    return Mono.when(titleMono, rawBodyMono, htmlBodyMono)
                        .then(Mono.fromSupplier(builder::build));
                })
            );
    }

    @Builder
    record NotificationContent(String title, String rawBody, String htmlBody, ReasonType reasonType,
                               ReasonAttributes reasonAttributes) {
    }

    String getUnsubscribeUrl(Subscription subscription) {
        return subscriptionRouter.getUnsubscribeUrl(subscription);
    }

    @Builder
    record NotificationElement(ReasonType reasonType, Reason reason,
                               Subscription subscription, NotifierDescriptor descriptor,
                               String notificationTitle,
                               String notificationRawBody,
                               String notificationHtmlBody,
                               ReasonAttributes reasonAttributes) {
    }

    Mono<ReasonType> getReasonType(String reasonTypeName) {
        return client.get(ReasonType.class, reasonTypeName);
    }

    Mono<Locale> getLocaleFromSubscriber(Subscription subscription) {
        // TODO get locale from subscriber
        return Mono.just(Locale.getDefault());
    }

    Flux<Subscription> listObservers(String reasonTypeName,
        Subscription.ReasonSubject reasonSubject) {
        Assert.notNull(reasonTypeName, "The reasonTypeName must not be null");
        Assert.notNull(reasonSubject, "The reasonSubject must not be null");
        final var listOptions = new ListOptions();
        var fieldQuery = getSubscriptionFieldQuery(reasonTypeName, reasonSubject);
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return distinctByKey(client.listAll(Subscription.class, listOptions, defaultSort()));
    }

    private static Query getSubscriptionFieldQuery(String reasonTypeName,
        Subscription.ReasonSubject reasonSubject) {
        var matchAllSubject = new Subscription.ReasonSubject();
        matchAllSubject.setKind(reasonSubject.getKind());
        matchAllSubject.setApiVersion(reasonSubject.getApiVersion());
        return and(equal("spec.reason.reasonType", reasonTypeName),
            or(equal("spec.reason.subject", reasonSubject.toString()),
                // source reason subject name is blank present match all
                equal("spec.reason.subject", matchAllSubject.toString())
            )
        );
    }

    static Flux<Subscription> distinctByKey(Flux<Subscription> source) {
        final var distinctKeyPredicate = subscriptionDistinctKeyPredicate();
        return source.distinct(Function.identity(), HashSet<Subscription>::new,
            (set, val) -> {
                for (Subscription subscription : set) {
                    if (distinctKeyPredicate.test(subscription, val)) {
                        return false;
                    }
                }
                // no duplicated return true
                set.add(val);
                return true;
            },
            HashSet::clear);
    }

    Sort defaultSort() {
        return Sort.by(Sort.Order.asc("metadata.creationTimestamp"),
            Sort.Order.asc("metadata.name"));
    }

    static BiPredicate<Subscription, Subscription> subscriptionDistinctKeyPredicate() {
        return (a, b) -> {
            if (!a.getSpec().getSubscriber().equals(b.getSpec().getSubscriber())) {
                return false;
            }
            var reasonA = a.getSpec().getReason();
            var reasonB = b.getSpec().getReason();
            if (!reasonA.getReasonType().equals(reasonB.getReasonType())) {
                return false;
            }
            var ars = reasonA.getSubject();
            var brs = reasonB.getSubject();
            var gvkForA =
                GroupVersionKind.fromAPIVersionAndKind(ars.getApiVersion(), ars.getKind());
            var gvkForB =
                GroupVersionKind.fromAPIVersionAndKind(brs.getApiVersion(), brs.getKind());

            if (!gvkForA.groupKind().equals(gvkForB.groupKind())) {
                return false;
            }
            // name is blank present match all
            if (StringUtils.isBlank(ars.getName()) || StringUtils.isBlank(brs.getName())) {
                return true;
            }
            return ars.getName().equals(brs.getName());
        };
    }
}
