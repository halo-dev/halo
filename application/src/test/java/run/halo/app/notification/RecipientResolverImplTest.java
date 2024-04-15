package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link RecipientResolverImpl}.
 *
 * @author guqing
 * @since 2.15.0
 */
@ExtendWith(MockitoExtension.class)
class RecipientResolverImplTest {

    @Mock
    private ReactiveExtensionClient client;

    private RecipientResolverImpl recipientResolver;

    @BeforeEach
    void setUp() {
        recipientResolver = spy(new RecipientResolverImpl(client));
    }

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

        doReturn(Flux.just(subscription1, subscription2))
            .when(recipientResolver).listSubscriptions(anyString());

        recipientResolver.resolve(reason)
            .as(StepVerifier::create)
            .expectNext(new Subscriber(UserIdentity.of("guqing"), "guqing-subscription"))
            .verifyComplete();

        verify(recipientResolver).listSubscriptions(any());
        verify(recipientResolver).resolve(eq(reason));
    }

    @Test
    void testSubjectMatch() {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName("test");
        Subscription subscription = createSubscription(subscriber);

        doReturn(Flux.just(subscription))
            .when(recipientResolver).listSubscriptions(any());

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

        verify(recipientResolver).listSubscriptions(any());
        verify(recipientResolver).resolve(eq(reason));
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

        doReturn(Flux.just(subscription1, subscription2))
            .when(recipientResolver).listSubscriptions(any());

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

        verify(recipientResolver).listSubscriptions(any());
        verify(recipientResolver).resolve(eq(reason));
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
