package run.halo.app.event.comment;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Comment new event.
 *
 * @author johnniang
 * @date 19-4-23
 */
public abstract class CommentBaseEvent extends ApplicationEvent {

    /**
     * Comment id.
     */
    private final Long commentId;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source    the object on which the event initially occurred (never {@code null})
     * @param commentId comment id
     */
    public CommentBaseEvent(Object source, @NonNull Long commentId) {
        super(source);

        Assert.notNull(commentId, "Comment id must not be null");
        this.commentId = commentId;
    }

    @NonNull
    public Long getCommentId() {
        return commentId;
    }
}
