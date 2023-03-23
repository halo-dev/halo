package run.halo.app.event.post;

/**
 * Upvote event.
 *
 * @author guqing
 * @since 2.0.0
 */
public class UpvotedEvent extends VotedEvent {

    public UpvotedEvent(Object source, String group, String name, String plural) {
        super(source, group, name, plural);
    }
}
