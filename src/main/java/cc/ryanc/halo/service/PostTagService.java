package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.entity.PostTag;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Post tag service interface.
 *
 * @author johnniang
 * @date 3/19/19
 */
public interface PostTagService extends CrudService<PostTag, Integer> {

    /**
     * Lists tags by post id.
     *
     * @param postId post id must not be null
     * @return a list of tag
     */
    @NonNull
    List<Tag> listTagBy(@NonNull Integer postId);

    /**
     * Lists tag list map by post id.
     *
     * @param postIds post id collection
     * @return tag map (key: postId, value: a list of tags)
     */
    @NonNull
    Map<Integer, List<Tag>> listTagListMapBy(Collection<Integer> postIds);

    /**
     * Lists post by tag id.
     *
     * @param tagId tag id must not be null
     * @return a list of post
     */
    @NonNull
    List<Post> listPostBy(@NonNull Integer tagId);

}
