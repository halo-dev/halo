package run.halo.app.event.post;

/**
 * Post visit event.
 *
 * @author johnniang
 * @date 19-4-22
 */
public class PostVisitEvent extends AbstractVisitEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param postId post id must not be null
     */
    public PostVisitEvent(Object source, Integer postId) {
        super(source, postId);
    }
}
