package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link RecipientResolverImpl}.
 *
 * @author guqing
 * @since 2.15.0
 */
@ExtendWith(MockitoExtension.class)
class RecipientResolverImplTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private RecipientResolverImpl recipientResolver;

    @Test
    void testExpressionMatch() {
        var subscriber1 = new Subscription.Subscriber();
        subscriber1.setName("test");
        final var subscription1 = createSubscription(subscriber1);
        subscription1.getMetadata().setName("test-subscription");
        subscription1.getSpec().getReason().setSubject(null);
        subscription1.getSpec().getReason().setExpression("props.owner == 'test'");

        var subscriber2 = new Subscription.Subscriber();
        subscriber2.setName("guqing");
        final var subscription2 = createSubscription(subscriber2);
        subscription2.getMetadata().setName("guqing-subscription");
        subscription2.getSpec().getReason().setSubject(null);
        subscription2.getSpec().getReason().setExpression("props.owner == 'guqing'");

        var reason = new Reason();
        reason.setSpec(new Reason.Spec());
        reason.getSpec().setReasonType("new-comment-on-post");
        reason.getSpec().setSubject(new Reason.Subject());
        reason.getSpec().getSubject().setApiVersion("content.halo.run/v1alpha1");
        reason.getSpec().getSubject().setKind("Post");
        reason.getSpec().getSubject().setName("fake-post");
        var reasonAttributes = new ReasonAttributes();
        reasonAttributes.put("owner", "guqing");
        reason.getSpec().setAttributes(reasonAttributes);

        when(subscriptionService.listByPerPage(anyString()))
            .thenReturn(Flux.just(subscription1, subscription2));

        recipientResolver.resolve(reason)
            .as(StepVerifier::create)
            .expectNext(new Subscriber(UserIdentity.of("guqing"), "guqing-subscription"))
            .verifyComplete();

        verify(subscriptionService).listByPerPage(anyString());
    }

    @Test
    void testSubjectMatch() {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName("test");
        Subscription subscription = createSubscription(subscriber);

        when(subscriptionService.listByPerPage(anyString()))
            .thenReturn(Flux.just(subscription));

        var reason = new Reason();
        reason.setSpec(new Reason.Spec());
        reason.getSpec().setReasonType("new-comment-on-post");
        reason.getSpec().setSubject(new Reason.Subject());
        reason.getSpec().getSubject().setApiVersion("content.halo.run/v1alpha1");
        reason.getSpec().getSubject().setKind("Post");
        reason.getSpec().getSubject().setName("fake-post");

        recipientResolver.resolve(reason)
            .as(StepVerifier::create)
            .expectNext(new Subscriber(UserIdentity.of("test"), "fake-subscription"))
            .verifyComplete();

        verify(subscriptionService).listByPerPage(anyString());
    }

    @Test
    void distinct() {
        // same subscriber to different subscriptions
        var subscriber = new Subscription.Subscriber();
        subscriber.setName("test");

        final var subscription1 = createSubscription(subscriber);
        subscription1.getMetadata().setName("sub-1");

        final var subscription2 = createSubscription(subscriber);
        subscription2.getMetadata().setName("sub-2");
        subscription2.getSpec().getReason().setSubject(null);
        subscription2.getSpec().getReason().setExpression("props.owner == 'guqing'");

        when(subscriptionService.listByPerPage(anyString()))
            .thenReturn(Flux.just(subscription1, subscription2));

        var reason = new Reason();
        reason.setSpec(new Reason.Spec());
        reason.getSpec().setReasonType("new-comment-on-post");
        reason.getSpec().setSubject(new Reason.Subject());
        reason.getSpec().getSubject().setApiVersion("content.halo.run/v1alpha1");
        reason.getSpec().getSubject().setKind("Post");
        reason.getSpec().getSubject().setName("fake-post");
        var reasonAttributes = new ReasonAttributes();
        reasonAttributes.put("owner", "guqing");
        reason.getSpec().setAttributes(reasonAttributes);

        recipientResolver.resolve(reason)
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();

        verify(subscriptionService).listByPerPage(anyString());
    }

    @Test
    void subjectMatchTest() {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName("test");

        final var subscription = createSubscription(subscriber);

        // match all name subscription
        var subject = new Reason.Subject();
        subject.setApiVersion("content.halo.run/v1alpha1");
        subject.setKind("Post");
        subject.setName("fake-post");
        assertThat(RecipientResolverImpl.subjectMatch(subscription, subject)).isTrue();

        // different kind
        subject = new Reason.Subject();
        subject.setApiVersion("content.halo.run/v1alpha1");
        subject.setKind("SinglePage");
        subject.setName("fake-post");
        assertThat(RecipientResolverImpl.subjectMatch(subscription, subject)).isFalse();

        // special case
        subscription.getSpec().getReason().getSubject().setName("other-post");
        subject = new Reason.Subject();
        subject.setApiVersion("content.halo.run/v1alpha1");
        subject.setKind("Post");
        subject.setName("fake-post");
        assertThat(RecipientResolverImpl.subjectMatch(subscription, subject)).isFalse();
        subject.setName("other-post");
        assertThat(RecipientResolverImpl.subjectMatch(subscription, subject)).isTrue();
    }

    private static Subscription createSubscription(Subscription.Subscriber subscriber) {
        Subscription subscription = new Subscription();
        subscription.setMetadata(new Metadata());
        subscription.getMetadata().setName("fake-subscription");
        subscription.setSpec(new Subscription.Spec());
        subscription.getSpec().setSubscriber(subscriber);

        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType("new-comment-on-post");
        interestReason.setSubject(new Subscription.ReasonSubject());
        interestReason.getSubject().setApiVersion("content.halo.run/v1alpha1");
        interestReason.getSubject().setKind("Post");
        subscription.getSpec().setReason(interestReason);
        return subscription;
    }
}
