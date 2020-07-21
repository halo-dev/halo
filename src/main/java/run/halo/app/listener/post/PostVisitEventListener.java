package run.halo.app.listener.post;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.event.post.PostVisitEvent;
import run.halo.app.handler.read.impl.LocalCacheRead;
import run.halo.app.model.entity.Post;
import run.halo.app.service.PostService;

/**
 * Visit event listener.
 *
 * @author johnniang
 * @date 19-4-22
 * @author HeHui
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
