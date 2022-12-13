package run.halo.app.event.post;

/**
 * Downvote event.
 *
 * @author guqing
 * @since 2.0.0
 */
public class DownvotedEvent extends VotedEvent {

    public DownvotedEvent(Object source, String group, String name, String plural) {
        super(source, group, name, plural);
    }
}
