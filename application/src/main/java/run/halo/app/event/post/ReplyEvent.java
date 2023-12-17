package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.content.Reply;

/**
 * @author guqing
 * @since 2.0.0
 */
public abstract class ReplyEvent extends ApplicationEvent {
    private final Reply reply;

    public ReplyEvent(Object source, Reply reply) {
        super(source);
        this.reply = reply;
    }

    public Reply getReply() {
        return reply;
    }
}
