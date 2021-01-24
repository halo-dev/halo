package run.halo.app.event.comment;

import org.springframework.lang.NonNull;

/**
 * PostComment reply event.
 *
 * @author johnniang
 * @date 19-4-23
 */
public class CommentReplyEvent extends AbstractCommentBaseEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param commentId comment id
     */
    public CommentReplyEvent(Object source, @NonNull Long commentId) {
        super(source, commentId);
    }
}
