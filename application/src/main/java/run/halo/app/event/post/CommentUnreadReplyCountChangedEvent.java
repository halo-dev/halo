package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * <p>This event will be triggered when the unread reply count of the comment is changed.</p>
 * <p>It is used to update the unread reply count of the comment,such as when the user reads the
 * reply(lastReadTime changed in comment), the unread reply count will be updated.</p>
 *
 * @author guqing
 * @since 2.14.0
 */
@Getter
public class CommentUnreadReplyCountChangedEvent extends ApplicationEvent {
    private final String commentName;

    public CommentUnreadReplyCountChangedEvent(Object source, String commentName) {
        super(source);
        this.commentName = commentName;
    }
}
