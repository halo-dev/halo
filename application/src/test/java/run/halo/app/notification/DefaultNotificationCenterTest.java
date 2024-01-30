package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.core.extension.notification.NotificationTemplate;
import run.halo.app.core.extension.notification.NotifierDescriptor;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link DefaultNotificationCenter}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultNotificationCenterTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private ReasonNotificationTemplateSelector notificationTemplateSelector;

    @Mock
    private UserNotificationPreferenceService userNotificationPreferenceService;

    @Mock
    private NotificationTemplateRender notificationTemplateRender;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private DefaultNotificationCenter notificationCenter;

    @Test
    public void testNotify() {
        final Reason reason = new Reason();
        final Reason.Spec spec = new Reason.Spec();
        Reason.Subject subject = new Reason.Subject();
        subject.setApiVersion("content.halo.run/v1alpha1");
        subject.setKind("Comment");
        subject.setName("comment-a");
        spec.setSubject(subject);
        spec.setReasonType("new-reply-on-comment");
        spec.setAttributes(null);
        reason.setSpec(spec);
        reason.setMetadata(new Metadata());
        reason.getMetadata().setName("reason-a");

        var subscriptionReasonSubject = createNewReplyOnCommentSubject();

        var spyNotificationCenter = spy(notificationCenter);
        var subscriptions = createSubscriptions();
        doReturn(Flux.fromIterable(subscriptions))
            .when(spyNotificationCenter).listObservers(eq("new-reply-on-comment"),
                eq(subscriptionReasonSubject));
        doReturn(Mono.empty()).when(spyNotificationCenter)
            .dispatchNotification(eq(reason), any());

        spyNotificationCenter.notify(reason).block();

        verify(spyNotificationCenter).dispatchNotification(eq(reason), any());
        verify(spyNotificationCenter).listObservers(eq("new-reply-on-comment"),
            eq(subscriptionReasonSubject));
    }

    List<Subscription> createSubscriptions() {
        Subscription subscription = new Subscription();
        subscription.setMetadata(new Metadata());
        subscription.getMetadata().setName("subscription-a");

        subscription.setSpec(new Subscription.Spec());
        subscription.getSpec().setSubscriber(new Subscription.Subscriber());
        subscription.getSpec().getSubscriber().setName("anonymousUser#A");

        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType("new-reply-on-comment");
        interestReason.setSubject(createNewReplyOnCommentSubject());
        subscription.getSpec().setReason(interestReason);

        return List.of(subscription);
    }

    Subscription.ReasonSubject createNewReplyOnCommentSubject() {
        var reasonSubject = new Subscription.ReasonSubject();
        reasonSubject.setApiVersion("content.halo.run/v1alpha1");
        reasonSubject.setKind("Comment");
        reasonSubject.setName("comment-a");
        return reasonSubject;
    }

    @Test
    public void testSubscribe() {
        var spyNotificationCenter = spy(notificationCenter);
        Subscription subscription = createSubscriptions().get(0);

        var subscriber = subscription.getSpec().getSubscriber();

        doReturn(Flux.just(subscription))
            .when(spyNotificationCenter).listSubscription(eq(subscriber));

        var reason = subscription.getSpec().getReason();

        when(client.create(any(Subscription.class))).thenReturn(Mono.empty());

        spyNotificationCenter.subscribe(subscriber, reason).block();

        verify(client, times(0)).create(any(Subscription.class));

        // not exists subscription will create a new subscription
        var newReason = JsonUtils.deepCopy(reason);
        newReason.setReasonType("fake-reason-type");
        spyNotificationCenter.subscribe(subscriber, newReason).block();
        verify(client).create(any(Subscription.class));
    }


    @Test
    public void testUnsubscribe() {
        Subscription.Subscriber subscriber = new Subscription.Subscriber();
        subscriber.setName("anonymousUser#A");
        var spyNotificationCenter = spy(notificationCenter);
        var subscriptions = createSubscriptions();
        doReturn(Flux.fromIterable(subscriptions))
            .when(spyNotificationCenter).listSubscription(eq(subscriber));

        when(client.delete(any(Subscription.class))).thenReturn(Mono.empty());

        spyNotificationCenter.unsubscribe(subscriber).block();

        verify(client).delete(any(Subscription.class));
    }


    @Test
    public void testUnsubscribeWithReason() {
        var spyNotificationCenter = spy(notificationCenter);
        var subscriptions = createSubscriptions();

        var subscription = subscriptions.get(0);

        var subscriber = subscription.getSpec().getSubscriber();
        doReturn(Flux.just(subscription))
            .when(spyNotificationCenter).listSubscription(eq(subscriber));

        var reason = subscription.getSpec().getReason();

        var newReason = JsonUtils.deepCopy(reason);
        newReason.setReasonType("fake-reason-type");
        when(client.delete(any(Subscription.class))).thenReturn(Mono.empty());
        spyNotificationCenter.unsubscribe(subscriber, newReason).block();
        verify(client, times(0)).delete(any(Subscription.class));

        // exists subscription will be deleted
        spyNotificationCenter.unsubscribe(subscriber, reason).block();
        verify(client).delete(any(Subscription.class));
    }

    @Test
    public void testGetNotifiersBySubscriber() {
        UserNotificationPreference preference = new UserNotificationPreference();
        when(userNotificationPreferenceService.getByUser(any()))
            .thenReturn(Mono.just(preference));

        var reason = new Reason();
        reason.setMetadata(new Metadata());
        reason.getMetadata().setName("reason-a");
        reason.setSpec(new Reason.Spec());
        reason.getSpec().setReasonType("new-reply-on-comment");
        var subscriber = new Subscription.Subscriber();
        subscriber.setName("anonymousUser#A");

        notificationCenter.getNotifiersBySubscriber(subscriber, reason)
            .collectList()
            .as(StepVerifier::create)
            .consumeNextWith(notifiers -> {
                assertThat(notifiers).hasSize(1);
                assertThat(notifiers.get(0)).isEqualTo("default-email-notifier");
            })
            .verifyComplete();

        verify(userNotificationPreferenceService).getByUser(eq(subscriber.getName()));
    }

    @Test
    public void testDispatchNotification() {
        var spyNotificationCenter = spy(notificationCenter);

        doReturn(Flux.just("email-notifier"))
            .when(spyNotificationCenter).getNotifiersBySubscriber(any(), any());

        NotifierDescriptor notifierDescriptor = mock(NotifierDescriptor.class);
        when(client.fetch(eq(NotifierDescriptor.class), eq("email-notifier")))
            .thenReturn(Mono.just(notifierDescriptor));

        var notificationElement = mock(DefaultNotificationCenter.NotificationElement.class);
        doReturn(Mono.just(notificationElement))
            .when(spyNotificationCenter).prepareNotificationElement(any(), any(), any());

        doReturn(Mono.empty()).when(spyNotificationCenter).sendNotification(any());
        doReturn(Mono.empty()).when(spyNotificationCenter).createNotification(any());

        var reason = new Reason();
        reason.setMetadata(new Metadata());
        reason.getMetadata().setName("reason-a");
        reason.setSpec(new Reason.Spec());
        reason.getSpec().setReasonType("new-reply-on-comment");

        var subscription = createSubscriptions().get(0);
        spyNotificationCenter.dispatchNotification(reason, subscription).block();

        verify(client).fetch(eq(NotifierDescriptor.class), eq("email-notifier"));
        verify(spyNotificationCenter).sendNotification(any());
        verify(spyNotificationCenter).createNotification(any());
    }

    @Test
    public void testPrepareNotificationElement() {
        var spyNotificationCenter = spy(notificationCenter);

        doReturn(Mono.just(Locale.getDefault()))
            .when(spyNotificationCenter).getLocaleFromSubscriber(any());

        var notificationContent = mock(DefaultNotificationCenter.NotificationContent.class);
        doReturn(Mono.just(notificationContent))
            .when(spyNotificationCenter).inferenceTemplate(any(), any(), any());

        spyNotificationCenter.prepareNotificationElement(any(), any(), any())
            .block();

        verify(spyNotificationCenter).getLocaleFromSubscriber(any());
        verify(spyNotificationCenter).inferenceTemplate(any(), any(), any());
    }

    @Test
    public void testSendNotification() {
        var spyNotificationCenter = spy(notificationCenter);

        var context = mock(NotificationContext.class);
        doReturn(Mono.just(context))
            .when(spyNotificationCenter).notificationContextFrom(any());

        when(notificationSender.sendNotification(eq("fake-notifier-ext"), any()))
            .thenReturn(Mono.empty());

        var element = mock(DefaultNotificationCenter.NotificationElement.class);
        var mockDescriptor = mock(NotifierDescriptor.class);
        when(element.descriptor()).thenReturn(mockDescriptor);
        when(element.subscription()).thenReturn(mock(Subscription.class));
        var notifierDescriptorSpec = mock(NotifierDescriptor.Spec.class);
        when(mockDescriptor.getSpec()).thenReturn(notifierDescriptorSpec);
        when(notifierDescriptorSpec.getNotifierExtName()).thenReturn("fake-notifier-ext");

        spyNotificationCenter.sendNotification(element).block();

        verify(spyNotificationCenter).notificationContextFrom(any());
        verify(notificationSender).sendNotification(any(), any());
    }

    @Test
    public void testCreateNotification() {
        var element = mock(DefaultNotificationCenter.NotificationElement.class);
        var subscription = createSubscriptions().get(0);
        var user = mock(User.class);

        var subscriberName = subscription.getSpec().getSubscriber().getName();
        when(client.fetch(eq(User.class), eq(subscriberName))).thenReturn(Mono.just(user));
        when(element.subscription()).thenReturn(subscription);

        when(client.create(any(Notification.class))).thenReturn(Mono.empty());

        var reason = new Reason();
        reason.setMetadata(new Metadata());
        reason.getMetadata().setName("reason-a");
        reason.setSpec(new Reason.Spec());
        reason.getSpec().setReasonType("new-reply-on-comment");
        when(element.reason()).thenReturn(reason);

        notificationCenter.createNotification(element).block();

        verify(client).fetch(eq(User.class), eq(subscriberName));
        verify(client).create(any(Notification.class));
    }

    @Test
    public void testInferenceTemplate() {
        final var spyNotificationCenter = spy(notificationCenter);

        final var reasonType = mock(ReasonType.class);

        var reason = new Reason();
        reason.setMetadata(new Metadata());
        reason.getMetadata().setName("reason-a");
        reason.setSpec(new Reason.Spec());
        reason.getSpec().setReasonType("new-reply-on-comment");

        var reasonTypeName = reason.getSpec().getReasonType();

        doReturn(Mono.just(reasonType))
            .when(spyNotificationCenter).getReasonType(eq(reasonTypeName));
        doReturn("fake-url")
            .when(spyNotificationCenter).getUnsubscribeUrl(any());

        final var locale = Locale.CHINESE;

        var template = new NotificationTemplate();
        template.setMetadata(new Metadata());
        template.getMetadata().setName("notification-template-a");
        template.setSpec(new NotificationTemplate.Spec());
        template.getSpec().setTemplate(new NotificationTemplate.Template());
        template.getSpec().getTemplate().setRawBody("body");
        template.getSpec().getTemplate().setHtmlBody("html-body");
        template.getSpec().getTemplate().setTitle("title");
        template.getSpec().setReasonSelector(new NotificationTemplate.ReasonSelector());
        template.getSpec().getReasonSelector().setReasonType(reasonTypeName);
        template.getSpec().getReasonSelector().setLanguage(locale.getLanguage());

        when(notificationTemplateRender.render(anyString(), any()))
            .thenReturn(Mono.empty());
        when(notificationTemplateSelector.select(eq(reasonTypeName), any()))
            .thenReturn(Mono.just(template));

        var subscription = new Subscription();
        subscription.setSpec(new Subscription.Spec());
        var subscriber = new Subscription.Subscriber();
        subscriber.setName("anonymousUser#A");
        subscription.getSpec().setSubscriber(subscriber);

        spyNotificationCenter.inferenceTemplate(reason, subscription, locale).block();

        verify(spyNotificationCenter).getReasonType(eq(reasonTypeName));
        verify(notificationTemplateSelector).select(eq(reasonTypeName), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listSubscriptionTest() {
        var subscriptions = createSubscriptions();

        when(client.list(eq(Subscription.class), any(Predicate.class), any()))
            .thenAnswer(answer -> {
                var predicate = (Predicate<Subscription>) answer.getArgument(1, Predicate.class);
                return Flux.fromIterable(subscriptions)
                    .filter(predicate);
            });

        var subscription = subscriptions.get(0);
        var subscriber = subscription.getSpec().getSubscriber();
        notificationCenter.listSubscription(subscriber)
            .as(StepVerifier::create)
            .expectNext(subscription)
            .verifyComplete();

        verify(client).list(eq(Subscription.class), any(Predicate.class), any());

        var otherSubscriber = JsonUtils.deepCopy(subscriber);
        otherSubscriber.setName("other");
        notificationCenter.listSubscription(otherSubscriber)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void listObserversTest() {
        var subscriptions = createSubscriptions();

        when(client.list(eq(Subscription.class), any(Predicate.class), any()))
            .thenAnswer(answer -> {
                var predicate = (Predicate<Subscription>) answer.getArgument(1, Predicate.class);
                return Flux.fromIterable(subscriptions)
                    .filter(predicate);
            });

        var subscription = subscriptions.get(0);
        var reasonTypeName = subscription.getSpec().getReason().getReasonType();
        var reasonSubject = subscription.getSpec().getReason().getSubject();
        notificationCenter.listObservers(reasonTypeName, reasonSubject)
            .as(StepVerifier::create)
            .expectNext(subscription)
            .verifyComplete();

        verify(client).list(eq(Subscription.class), any(Predicate.class), any());

        notificationCenter.listObservers("other-reason", reasonSubject)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void listObserverWhenDuplicateSubscribers() {
        var sourceSubscriptions = createSubscriptions();
        var subscriptionA = sourceSubscriptions.get(0);
        var subscriptionB = JsonUtils.deepCopy(subscriptionA);

        var subscriptionC = JsonUtils.deepCopy(subscriptionA);
        subscriptionC.getSpec().getReason().getSubject().setName(null);

        var subscriptions = List.of(subscriptionA, subscriptionB, subscriptionC);
        when(client.list(eq(Subscription.class), any(Predicate.class), any()))
            .thenAnswer(answer -> {
                var predicate = (Predicate<Subscription>) answer.getArgument(1, Predicate.class);
                return Flux.fromIterable(subscriptions)
                    .filter(predicate);
            });

        var subscription = subscriptions.get(0);
        var reasonTypeName = subscription.getSpec().getReason().getReasonType();
        var reasonSubject = subscription.getSpec().getReason().getSubject();
        notificationCenter.listObservers(reasonTypeName, reasonSubject)
            .as(StepVerifier::create)
            .expectNext(subscription)
            .verifyComplete();

        verify(client).list(eq(Subscription.class), any(Predicate.class), any());

        notificationCenter.listObservers("other-reason", reasonSubject)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Nested
    class SubscriptionDistinctKeyPredicateTest {

        @Test
        void differentSubjectName() {
            var subscriptionA = createSubscriptions().get(0);
            var subscriptionB = JsonUtils.deepCopy(subscriptionA);
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isTrue();

            subscriptionB.getSpec().getReason().getSubject().setName("other");
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isFalse();

            subscriptionB.getSpec().getReason().getSubject().setName(null);
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isTrue();
        }

        @Test
        void differentSubjectApiVersion() {
            var subscriptionA = createSubscriptions().get(0);
            var subscriptionB = JsonUtils.deepCopy(subscriptionA);
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isTrue();

            var subjectA = subscriptionA.getSpec().getReason().getSubject();
            var gvForA = GroupVersion.parseAPIVersion(subjectA.getApiVersion());
            subscriptionB.getSpec().getReason().getSubject()
                .setApiVersion(gvForA.group() + "/otherVersion");
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isTrue();
        }

        @Test
        void differentReasonType() {
            var subscriptionA = createSubscriptions().get(0);
            var subscriptionB = JsonUtils.deepCopy(subscriptionA);
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isTrue();

            subscriptionB.getSpec().getReason().setReasonType("other");
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isFalse();
        }

        @Test
        void differentSubscriber() {
            var subscriptionA = createSubscriptions().get(0);
            var subscriptionB = JsonUtils.deepCopy(subscriptionA);
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isTrue();

            subscriptionB.getSpec().getSubscriber().setName("other");
            assertThat(DefaultNotificationCenter.subscriptionDistinctKeyPredicate()
                .test(subscriptionA, subscriptionB)).isFalse();
        }
    }

    @Test
    void getLocaleFromSubscriberTest() {
        var subscription = mock(Subscription.class);

        notificationCenter.getLocaleFromSubscriber(subscription)
            .as(StepVerifier::create)
            .expectNext(Locale.getDefault())
            .verifyComplete();
    }
}
