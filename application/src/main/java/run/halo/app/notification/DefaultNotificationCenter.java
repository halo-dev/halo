package run.halo.app.notification;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.core.extension.notification.NotifierDescriptor;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
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
    private final RecipientResolver recipientResolver;
    private final SubscriptionService subscriptionService;

    @Override
    public Mono<Void> notify(Reason reason) {
        return recipientResolver.resolve(reason)
            .doOnNext(subscriber -> {
                log.debug("Dispatching notification to subscriber [{}] for reason [{}]",
                    subscriber, reason.getMetadata().getName());
            })
            .publishOn(Schedulers.boundedElastic())
            .flatMap(subscriber -> dispatchNotification(reason, subscriber))
            .then();
    }

    @Override
    public Mono<Subscription> subscribe(Subscription.Subscriber subscriber,
        Subscription.InterestReason reason) {
        return unsubscribe(subscriber, reason)
            .then(Mono.defer(() -> {
                var subscription = new Subscription();
                subscription.setMetadata(new Metadata());
                subscription.getMetadata().setGenerateName("subscription-");
                subscription.setSpec(new Subscription.Spec());
                subscription.getSpec().setUnsubscribeToken(Subscription.generateUnsubscribeToken());
                subscription.getSpec().setSubscriber(subscriber);
                Subscription.InterestReason.ensureSubjectHasValue(reason);
                subscription.getSpec().setReason(reason);
                return client.create(subscription);
            }));
    }

    @Override
    public Mono<Void> unsubscribe(Subscription.Subscriber subscriber) {
        return subscriptionService.remove(subscriber).then();
    }

    @Override
    public Mono<Void> unsubscribe(Subscription.Subscriber subscriber,
        Subscription.InterestReason reason) {
        return subscriptionService.remove(subscriber, reason).then();
    }

    Flux<String> getNotifiersBySubscriber(Subscriber subscriber, Reason reason) {
        var reasonType = reason.getSpec().getReasonType();
        return userNotificationPreferenceService.getByUser(subscriber.name())
            .map(UserNotificationPreference::getReasonTypeNotifier)
            .map(reasonTypeNotification -> reasonTypeNotification.getNotifiers(reasonType))
            .flatMapMany(Flux::fromIterable);
    }

    Mono<Void> dispatchNotification(Reason reason, Subscriber subscriber) {
        return getNotifiersBySubscriber(subscriber, reason)
            .flatMap(notifierName -> client.fetch(NotifierDescriptor.class, notifierName))
            .flatMap(descriptor -> prepareNotificationElement(subscriber, reason, descriptor))
            .flatMap(element -> {
                var dispatchMono = sendNotification(element);
                if (subscriber.isAnonymous()) {
                    return dispatchMono;
                }
                // create notification for user
                var innerNofificationMono = createNotification(element);
                return Mono.when(dispatchMono, innerNofificationMono);
            })
            .then();
    }

    Mono<NotificationElement> prepareNotificationElement(Subscriber subscriber, Reason reason,
        NotifierDescriptor descriptor) {
        return getLocaleFromSubscriber(subscriber)
            .flatMap(locale -> inferenceTemplate(reason, subscriber, locale))
            .map(notificationContent -> NotificationElement.builder()
                .descriptor(descriptor)
                .reason(reason)
                .subscriber(subscriber)
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
        var subscriber = notificationElement.subscriber();
        final var notifierExtName = descriptor.getSpec().getNotifierExtName();
        return notificationContextFrom(notificationElement)
            .flatMap(notificationContext -> notificationSender.sendNotification(notifierExtName,
                    notificationContext)
                .onErrorResume(throwable -> {
                    log.error(
                        "Failed to send notification to subscriber [{}] through notifier [{}]",
                        subscriber,
                        descriptor.getSpec().getDisplayName(),
                        throwable);
                    return Mono.empty();
                })
            )
            .then();
    }

    Mono<Notification> createNotification(NotificationElement notificationElement) {
        var reason = notificationElement.reason();
        var subscriber = notificationElement.subscriber();
        return client.fetch(User.class, subscriber.name())
            .flatMap(user -> {
                Notification notification = new Notification();
                notification.setMetadata(new Metadata());
                notification.getMetadata().setGenerateName("notification-");
                notification.setSpec(new Notification.NotificationSpec());
                notification.getSpec().setTitle(notificationElement.notificationTitle());
                notification.getSpec().setRawContent(notificationElement.notificationRawBody());
                notification.getSpec().setHtmlContent(notificationElement.notificationHtmlBody);
                notification.getSpec().setRecipient(subscriber.name());
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
        final var subscriber = element.subscriber();

        var messagePayload = new NotificationContext.MessagePayload();
        messagePayload.setTitle(element.notificationTitle());
        messagePayload.setRawBody(element.notificationRawBody());
        messagePayload.setHtmlBody(element.notificationHtmlBody());
        messagePayload.setAttributes(element.reasonAttributes());

        var message = new NotificationContext.Message();
        message.setRecipient(subscriber.name());
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

    Mono<NotificationContent> inferenceTemplate(Reason reason, Subscriber subscriber,
        Locale locale) {
        var reasonTypeName = reason.getSpec().getReasonType();
        return getReasonType(reasonTypeName)
            .flatMap(reasonType -> notificationTemplateSelector.select(reasonTypeName, locale)
                .flatMap(template -> {
                    final var templateContent = template.getSpec().getTemplate();
                    var model = toReasonAttributes(reason);
                    var subscriberInfo = new HashMap<>();
                    if (subscriber.isAnonymous()) {
                        subscriberInfo.put("displayName", subscriber.getEmail().orElseThrow());
                    } else {
                        subscriberInfo.put("displayName", "@" + subscriber.username());
                    }
                    subscriberInfo.put("id", subscriber.name());
                    model.put("subscriber", subscriberInfo);

                    var unsubscriptionMono = getUnsubscribeUrl(subscriber.subscriptionName())
                        .doOnNext(url -> model.put("unsubscribeUrl", url));

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
                    return Mono.when(unsubscriptionMono, titleMono, rawBodyMono, htmlBodyMono)
                        .then(Mono.fromSupplier(builder::build));
                })
            );
    }

    @Builder
    record NotificationContent(String title, String rawBody, String htmlBody, ReasonType reasonType,
                               ReasonAttributes reasonAttributes) {
    }

    Mono<String> getUnsubscribeUrl(String subscriptionName) {
        return client.get(Subscription.class, subscriptionName)
            .map(subscriptionRouter::getUnsubscribeUrl);
    }

    @Builder
    record NotificationElement(ReasonType reasonType, Reason reason,
                               Subscriber subscriber, NotifierDescriptor descriptor,
                               String notificationTitle,
                               String notificationRawBody,
                               String notificationHtmlBody,
                               ReasonAttributes reasonAttributes) {
    }

    Mono<ReasonType> getReasonType(String reasonTypeName) {
        return client.get(ReasonType.class, reasonTypeName);
    }

    Mono<Locale> getLocaleFromSubscriber(Subscriber subscriber) {
        // TODO get locale from subscriber
        return Mono.just(Locale.getDefault());
    }
}
