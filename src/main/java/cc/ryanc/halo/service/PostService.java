package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.PostSimpleOutputDTO;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

/**
 * Post service.
 *
 * @author johnniang
 */
public interface PostService extends CrudService<Post, Integer> {

    /**
     * Lists latest posts.
     *
     * @param top top number must not be less than 0
     * @return latest posts
     */
    @NonNull
    Page<PostSimpleOutputDTO> listLatest(int top);

}
