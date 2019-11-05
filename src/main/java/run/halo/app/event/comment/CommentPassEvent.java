package run.halo.app.event.comment;

import org.springframework.lang.NonNull;

/**
 * PostComment pass event.
 *
 * @author johnniang
 * @date 19-4-23
 */
public class CommentPassEvent extends CommentBaseEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source    the object on which the event initially occurred (never {@code null})
     * @param commentId comment id
     */
    public CommentPassEvent(Object source, @NonNull Long commentId) {
        super(source, commentId);
    }
}
