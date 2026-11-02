package run.halo.app.event.post;

import run.halo.app.core.extension.content.Reply;

/**
 * Event published when a reply waiting for review gets approved.
 *
 * @since 2.28.0
 */
public class ReplyApprovedEvent extends ReplyEvent {

    public ReplyApprovedEvent(Object source, Reply reply) {
        super(source, reply);
    }
}
