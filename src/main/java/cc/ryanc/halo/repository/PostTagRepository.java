package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.PostTag;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;


/**
 * Post tag repository.
 *
 * @author johnniang
 */
public interface PostTagRepository extends BaseRepository<PostTag, Integer> {

    /**
     * Finds all post tags by post id.
     *
     * @param postId post id must not be null
     * @return a list of post tags
     */
    @NonNull
    List<PostTag> findAllByPostId(@NonNull Integer postId);

    /**
     * Finds all tag ids by post id.
     *
     * @param postId post id must not be null
     * @return a set of tag id
     */
    @Query("select postTag.tagId from PostTag postTag where postTag.postId = ?1")
    @NonNull
    Set<Integer> findAllTagIdsByPostId(@NonNull Integer postId);

    /**
     * Finds all post tags by tag id.
     *
     * @param tagId tag id must not be null
     * @return a list of post tags
     */
    @NonNull
    List<PostTag> findAllByTagId(@NonNull Integer tagId);

    /**
     * Finds all post id by tag id.
     *
     * @param tagId tag id must not be null
     * @return a set of post id
     */
    @Query("select postTag.postId from PostTag postTag where postTag.tagId = ?1")
    @NonNull
    Set<Integer> findAllPostIdsByTagId(@NonNull Integer tagId);

    /**
     * Finds all tags by post id in.
     *
     * @param postIds post id collection
     * @return a list of post tags
     */
    @NonNull
    List<PostTag> findAllByPostIdIn(@NonNull Iterable<Integer> postIds);

    /**
     * Deletes post tags by post id.
     *
     * @param postId post id must not be null
     * @return a list of post tag deleted
     */
    @NonNull
    List<PostTag> deleteByPostId(@NonNull Integer postId);

    /**
     * Deletes post tags by tag id.
     *
     * @param tagId tag id must not be null
     * @return a list of post tag deleted
     */
    @NonNull
    List<PostTag> deleteByTagId(@NonNull Integer tagId);
}
