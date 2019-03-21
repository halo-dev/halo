package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.TagWithCountOutputDTO;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.entity.PostTag;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    List<Tag> listTagsBy(@NonNull Integer postId);

    /**
     * List tag with post count output dtos.
     *
     * @param sort sort info
     * @return a list of tag with post count output dto
     */
    @NonNull
    List<TagWithCountOutputDTO> listTagWithCountDtos(@NonNull Sort sort);

    /**
     * Lists tags list map by post id.
     *
     * @param postIds post id collection
     * @return tag map (key: postId, value: a list of tags)
     */
    @NonNull
    Map<Integer, List<Tag>> listTagListMapBy(Collection<Integer> postIds);

    /**
     * Lists posts by tag id.
     *
     * @param tagId tag id must not be null
     * @return a list of post
     */
    @NonNull
    List<Post> listPostsBy(@NonNull Integer tagId);

    /**
     * Creates post tags by post id and tag id set.
     *
     * @param postId post id must not be null
     * @param tagIds tag id set
     * @return a list of post tag
     */
    @NonNull
    List<PostTag> createBy(@NonNull Integer postId, Set<Integer> tagIds);
}
