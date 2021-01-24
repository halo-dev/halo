package run.halo.app.event.comment;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * PostComment new event.
 *
 * @author johnniang
 * @date 19-4-23
 */
public abstract class AbstractCommentBaseEvent extends ApplicationEvent {

    /**
     * PostComment id.
     */
    private final Long commentId;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param commentId comment id
     */
    public AbstractCommentBaseEvent(Object source, @NonNull Long commentId) {
        super(source);

        Assert.notNull(commentId, "PostComment id must not be null");
        this.commentId = commentId;
    }

    @NonNull
    public Long getCommentId() {
        return commentId;
    }
}
