package run.halo.app.content.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.content.NotificationReasonConst;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.SubscriberEmailResolver;

/**
 * Reply notification subscription helper.
 *
 * @author guqing
 * @since 2.9.0
 */
@Component
@RequiredArgsConstructor
public class ReplyNotificationSubscriptionHelper {

    private final NotificationCenter notificationCenter;
    private final SubscriberEmailResolver subscriberEmailResolver;

    /**
     * Subscribe new reply reason for comment.
     *
     * @param comment comment
     */
    public void subscribeNewReplyReasonForComment(Comment comment) {
        var reasonSubject = Subscription.ReasonSubject.builder()
            .apiVersion(comment.getApiVersion())
            .kind(comment.getKind())
            .name(comment.getMetadata().getName())
            .build();
        subscribeReply(reasonSubject,
            Identity.fromCommentOwner(comment.getSpec().getOwner()));
    }

    /**
     * Subscribe new reply reason for reply.
     *
     * @param reply reply
     */
    public void subscribeNewReplyReasonForReply(Reply reply) {
        var reasonSubject = Subscription.ReasonSubject.builder()
            .apiVersion(reply.getApiVersion())
            .kind(reply.getKind())
            .name(reply.getMetadata().getName())
            .build();
        var subjectOwner = reply.getSpec().getOwner();
        subscribeReply(reasonSubject,
            Identity.fromCommentOwner(subjectOwner));
    }

    void subscribeReply(Subscription.ReasonSubject reasonSubject,
        Identity identity) {
        var subscriber = createSubscriber(identity);
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(NotificationReasonConst.SOMEONE_REPLIED_TO_YOU);
        interestReason.setSubject(reasonSubject);
        notificationCenter.subscribe(subscriber, interestReason).block();
    }

    private Subscription.Subscriber createSubscriber(Identity author) {
        Subscription.Subscriber subscriber;
        if (author.isEmail()) {
            subscriber = subscriberEmailResolver.ofEmail(author.name());
        } else {
            subscriber = new Subscription.Subscriber();
            subscriber.setName(author.name());
        }
        return subscriber;
    }

    record Identity(String name, boolean isEmail) {
        public static Identity fromCommentOwner(Comment.CommentOwner commentOwner) {
            if (Comment.CommentOwner.KIND_EMAIL.equals(commentOwner.getKind())) {
                return new Identity(commentOwner.getName(), true);
            }
            return new Identity(commentOwner.getName(), false);
        }
    }
}
