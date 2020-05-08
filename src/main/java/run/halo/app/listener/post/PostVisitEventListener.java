package run.halo.app.listener.post;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.event.post.PostVisitEvent;
import run.halo.app.model.entity.PostVisitIp;
import run.halo.app.service.PostService;
import run.halo.app.service.PostVisitIpService;

/**
 * Visit event listener.
 *
 * @author johnniang
 * @date 19-4-22
 */
@Component
public class PostVisitEventListener extends AbstractVisitEventListener {

    private final PostVisitIpService postVisitIpService;

    public PostVisitEventListener(PostService postService, PostVisitIpService postVisitIpService, PostVisitIpService postVisitIpService1) {
        super(postService);
        this.postVisitIpService = postVisitIpService1;
    }

    @Async
    @EventListener
    public void onPostVisitEvent(PostVisitEvent event) throws InterruptedException {
        handleVisitEvent(event);
    }

    /**
     * Check ip record by post id and request ip.
     *
     * @param postId    post id
     * @param requestIp request ip
     * @return whether ip has appeared previously
     */
    @Override
    public boolean checkIpRecord(Integer postId, String requestIp) {
        boolean appeared = postVisitIpService.checkIpRecordByPostIdAndIp(postId, requestIp);
        if (!appeared) {
            PostVisitIp visitIp = new PostVisitIp();
            visitIp.setPostId(postId);
            visitIp.setIpAddress(requestIp);
            postVisitIpService.createBy(visitIp);
            return false;
        } else {
            return true;
        }
    }
}
