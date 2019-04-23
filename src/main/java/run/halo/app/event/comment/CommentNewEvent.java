package run.halo.app.event.comment;

import org.springframework.lang.NonNull;

/**
 * Comment new event.
 *
 * @author johnniang
 * @date 19-4-23
 */
public class CommentNewEvent extends CommentBaseEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source    the object on which the event initially occurred (never {@code null})
     * @param commentId comment id
     */
    public CommentNewEvent(Object source, @NonNull Long commentId) {
        super(source, commentId);
    }
}
