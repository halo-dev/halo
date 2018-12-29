package cc.ryanc.halo.task;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : RYAN0UP
 * @date : 2018/12/5
 */
@Slf4j
public class PostSyncTask {

    /**
     * 将缓存的图文浏览数写入数据库
     */
    public void postSync() {
        final PostService postService = SpringUtil.getBean(PostService.class);
        Post post = null;
        int count = 0;
        for (Long key : HaloConst.POSTS_VIEWS.keySet()) {
            post = postService.findByPostId(key).orElse(null);
            if (null != post) {
                post.setPostViews(post.getPostViews() + HaloConst.POSTS_VIEWS.get(key));
                postService.save(post);
                count++;
            }
        }
        log.info("The number of visits to {} posts has been updated", count);
        HaloConst.POSTS_VIEWS.clear();
    }
}
