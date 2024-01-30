package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.content.NotificationReasonConst;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.SubscriberEmailResolver;

/**
 * Tests for {@link ReplyNotificationSubscriptionHelper}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class ReplyNotificationSubscriptionHelperTest {

    @Mock
    NotificationCenter notificationCenter;

    @Mock
    SubscriberEmailResolver subscriberEmailResolver;

    @InjectMocks
    ReplyNotificationSubscriptionHelper notificationSubscriptionHelper;

    @Test
    void subscribeNewReplyReasonForCommentTest() {
        var comment = createComment();
        var spyNotificationSubscriptionHelper = spy(notificationSubscriptionHelper);

        doNothing().when(spyNotificationSubscriptionHelper).subscribeReply(any(), any());

        spyNotificationSubscriptionHelper.subscribeNewReplyReasonForComment(comment);

        var reasonSubject = Subscription.ReasonSubject.builder()
            .apiVersion(comment.getApiVersion())
            .kind(comment.getKind())
            .name(comment.getMetadata().getName())
            .build();
        verify(spyNotificationSubscriptionHelper).subscribeReply(eq(reasonSubject),
            eq(ReplyNotificationSubscriptionHelper.Identity.fromCommentOwner(
                comment.getSpec().getOwner()))
        );
    }

    @Test
    void subscribeNewReplyReasonForReplyTest() {
        var reply = new Reply();
        reply.setMetadata(new Metadata());
        reply.getMetadata().setName("fake-reply");
        reply.setSpec(new Reply.ReplySpec());
        reply.getSpec().setCommentName("fake-comment");
        var owner = new Comment.CommentOwner();
        owner.setKind(User.KIND);
        owner.setName("fake-user");
        reply.getSpec().setOwner(owner);

        var spyNotificationSubscriptionHelper = spy(notificationSubscriptionHelper);

        doNothing().when(spyNotificationSubscriptionHelper).subscribeReply(any(), any());

        spyNotificationSubscriptionHelper.subscribeNewReplyReasonForReply(reply);

        var reasonSubject = Subscription.ReasonSubject.builder()
            .apiVersion(reply.getApiVersion())
            .kind(reply.getKind())
            .name(reply.getMetadata().getName())
            .build();
        verify(spyNotificationSubscriptionHelper).subscribeReply(eq(reasonSubject),
            eq(ReplyNotificationSubscriptionHelper.Identity.fromCommentOwner(
                reply.getSpec().getOwner()))
        );
    }

    @Test
    void subscribeReplyTest() {
        var comment = createComment();
        var reasonSubject = Subscription.ReasonSubject.builder()
            .apiVersion(comment.getApiVersion())
            .kind(comment.getKind())
            .name(comment.getMetadata().getName())
            .build();
        var identity = ReplyNotificationSubscriptionHelper.Identity.fromCommentOwner(
            comment.getSpec().getOwner());

        when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.empty());

        var subscriber = new Subscription.Subscriber();
        subscriber.setName(AnonymousUserConst.PRINCIPAL + "#" + identity.name());
        when(subscriberEmailResolver.ofEmail(eq(identity.name())))
            .thenReturn(subscriber);

        notificationSubscriptionHelper.subscribeReply(reasonSubject, identity);

        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(NotificationReasonConst.SOMEONE_REPLIED_TO_YOU);
        interestReason.setSubject(reasonSubject);
        verify(notificationCenter).subscribe(eq(subscriber), eq(interestReason));
        verify(subscriberEmailResolver).ofEmail(eq(identity.name()));
    }

    @Nested
    class IdentityTest {

        @Test
        void createForCommentOwner() {
            var commentOwner = new Comment.CommentOwner();
            commentOwner.setKind(Comment.CommentOwner.KIND_EMAIL);
            commentOwner.setName("example@example.com");

            var sub = ReplyNotificationSubscriptionHelper.Identity.fromCommentOwner(commentOwner);
            assertThat(sub.isEmail()).isTrue();
            assertThat(sub.name()).isEqualTo(commentOwner.getName());

            commentOwner.setKind(User.KIND);
            commentOwner.setName("fake-user");
            sub = ReplyNotificationSubscriptionHelper.Identity.fromCommentOwner(commentOwner);
            assertThat(sub.isEmail()).isFalse();
            assertThat(sub.name()).isEqualTo(commentOwner.getName());
        }
    }

    static Comment createComment() {
        var comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName("fake-comment");
        comment.setSpec(new Comment.CommentSpec());
        var commentOwner = new Comment.CommentOwner();
        commentOwner.setKind(Comment.CommentOwner.KIND_EMAIL);
        commentOwner.setName("example@example.com");
        comment.getSpec().setOwner(commentOwner);
        comment.getSpec().setSubjectRef(
            Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class)));
        return comment;
    }
}