package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.content.Comment;

/**
 * Comment created event.
 *
 * @author guqing
 * @since 2.9.0
 */
@Getter
public class CommentCreatedEvent extends ApplicationEvent {

    private final Comment comment;

    public CommentCreatedEvent(Object source, Comment comment) {
        super(source);
        this.comment = comment;
    }
}
