package run.halo.app.content.comment;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import run.halo.app.content.NotificationReasonConst;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.UserIdentity;

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

    /**
     * Subscribe new reply reason for comment.
     *
     * @param comment comment
     */
    public void subscribeNewReplyReasonForComment(Comment comment) {
        subscribeReply(identityFrom(comment.getSpec().getOwner()));
    }

    /**
     * Subscribe new reply reason for reply.
     *
     * @param reply reply
     */
    public void subscribeNewReplyReasonForReply(Reply reply) {
        var subjectOwner = reply.getSpec().getOwner();
        subscribeReply(identityFrom(subjectOwner));
    }

    void subscribeReply(UserIdentity identity) {
        var subscriber = createSubscriber(identity);
        if (subscriber == null) {
            return;
        }
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(NotificationReasonConst.SOMEONE_REPLIED_TO_YOU);
        interestReason.setExpression("props.repliedOwner == '%s'".formatted(identity.name()));
        notificationCenter.subscribe(subscriber, interestReason).block();
    }

    @Nullable
    private Subscription.Subscriber createSubscriber(UserIdentity author) {
        if (StringUtils.isBlank(author.name())) {
            return null;
        }

        Subscription.Subscriber subscriber = new Subscription.Subscriber();
        subscriber.setName(author.name());
        return subscriber;
    }

    public static UserIdentity identityFrom(Comment.CommentOwner owner) {
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return UserIdentity.anonymousWithEmail(owner.getName());
        }
        return UserIdentity.of(owner.getName());
    }
}
