package cc.ryanc.halo.task;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static cc.ryanc.halo.model.support.HaloConst.POSTS_VIEWS;

/**
 * @author : RYAN0UP
 * @date : 2018/12/5
 */
@Slf4j
@Component
public class PostSyncTask {

    private final PostService postService;

    public PostSyncTask(PostService postService) {
        this.postService = postService;
    }

    /**
     * 将缓存的图文浏览数写入数据库
     */
    @Scheduled(cron = "0 0 * * * *")
    public void postSync() {
        int count = 0;
        for (Long key : POSTS_VIEWS.keySet()) {
            Post post = postService.getByIdOfNullable(key);
            if (null != post) {
                post.setPostViews(post.getPostViews() + POSTS_VIEWS.get(key));
                postService.create(post);
                count++;
            }
        }
        log.info("The number of visits to {} posts has been updated", count);
        POSTS_VIEWS.clear();
    }
}
