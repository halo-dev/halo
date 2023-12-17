package run.halo.app.event.post;

import run.halo.app.core.extension.content.Reply;

public class ReplyDeletedEvent extends ReplyEvent {

    public ReplyDeletedEvent(Object source, Reply reply) {
        super(source, reply);
    }
}
