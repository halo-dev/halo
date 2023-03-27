package run.halo.app.event.post;

import run.halo.app.core.extension.content.Reply;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ReplyChangedEvent extends ReplyEvent {

    public ReplyChangedEvent(Object source, Reply reply) {
        super(source, reply);
    }
}
