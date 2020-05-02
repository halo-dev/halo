package run.halo.app.event.post;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.utils.ServiceUtils;

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
     * @param requestIp the ip of visitor
     * @param postId post id must not be null
     */
    public PostVisitEvent(Object source, @NonNull String requestIp, @NonNull Integer postId) {
        super(source, requestIp, postId);
        Assert.isTrue(!ServiceUtils.isEmptyId(postId), "Post id must not be empty");
    }
}
