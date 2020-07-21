package run.halo.app.listener.post;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.event.post.PostVisitEvent;
import run.halo.app.handler.read.impl.LocalCacheRead;
import run.halo.app.model.entity.Post;

/**
 * Visit event listener.
 *
 * @author johnniang
 * @author HeHui
 * @date 19-4-22
 * @date 2020-07-21 18:28
 */
@Component
public class PostVisitEventListener extends AbstractSmoothVisitEventListener {

    public PostVisitEventListener(LocalCacheRead<Post> read) {
        super(read);
    }

    @Async
    @EventListener
    public void onPostVisitEvent(PostVisitEvent event) throws InterruptedException {
        handleVisitEvent(event);
    }
}
