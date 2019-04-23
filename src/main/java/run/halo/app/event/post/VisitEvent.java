package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Visit event.
 *
 * @author johnniang
 * @date 19-4-22
 */
public class VisitEvent extends ApplicationEvent {

    private final Integer postId;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param postId post id
     */
    public VisitEvent(@NonNull Object source, @NonNull Integer postId) {
        super(source);

        Assert.notNull(postId, "Post id must not be null");
        this.postId = postId;
    }

    @NonNull
    public Integer getPostId() {
        return postId;
    }
}
