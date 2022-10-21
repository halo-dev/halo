package run.halo.app.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.projection.CategoryIdPostStatusProjection;
import run.halo.app.repository.base.BaseRepository;


/**
 * Post category repository.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-19
 */
public interface PostCategoryRepository extends BaseRepository<PostCategory, Integer> {

    /**
     * Finds all category ids by post id
     *
     * @param postId post id must not be null
     * @return a list of category id
     */
    @NonNull
    @Query("select postCategory.categoryId from PostCategory postCategory where postCategory"
        + ".postId = ?1")
    Set<Integer> findAllCategoryIdsByPostId(@NonNull Integer postId);

    /**
     * Finds all post ids by category id.
     *
     * @param categoryId category id must not be null
     * @return a set of post id
     */
    @NonNull
    @Query("select postCategory.postId from PostCategory postCategory where postCategory"
        + ".categoryId = ?1")
    Set<Integer> findAllPostIdsByCategoryId(@NonNull Integer categoryId);

    /**
     * Finds all post ids by category id and post status.
     *
     * @param categoryId category id must not be null
     * @param status post status must not be null
     * @return a set of post id
     */
    @NonNull
    @Query("select postCategory.postId from PostCategory postCategory, Post post where "
        + "postCategory.categoryId = ?1 and post.id = postCategory.postId and post.status = ?2")
    Set<Integer> findAllPostIdsByCategoryId(@NonNull Integer categoryId,
        @NonNull PostStatus status);

    /**
     * Finds all post ids by category id and post status.
     *
     * @param categoryId category id must not be null
     * @param status     post status must not be empty
     * @return a set of post id
     */
    @NonNull
    @Query("select postCategory.postId from PostCategory postCategory, Post post where"
        + " postCategory.categoryId = ?1 and post.id = postCategory.postId and post.status in (?2)")
    Set<Integer> findAllPostIdsByCategoryId(
        @NonNull Integer categoryId, @NonNull Set<PostStatus> status);

    /**
     * Finds all post categories by post id in.
     *
     * @param postIds post id collection must not be null
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> findAllByPostIdIn(@NonNull Collection<Integer> postIds);

    /**
     * Finds all post categories by post id.
     *
     * @param postId post id must not be null
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> findAllByPostId(@NonNull Integer postId);

    /**
     * Finds all post categories by category id.
     *
     * @param categoryId category id must not be null
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> findAllByCategoryId(@NonNull Integer categoryId);

    /**
     * Deletes post categories by post id.
     *
     * @param postId post id must not be null
     * @return a list of post category deleted
     */
    @NonNull
    List<PostCategory> deleteByPostId(@NonNull Integer postId);

    /**
     * Deletes post categories by category id.
     *
     * @param categoryId category id must not be null
     * @return a list of post category deleted
     */
    @NonNull
    List<PostCategory> deleteByCategoryId(@NonNull Integer categoryId);

    /**
     * Finds all post categories by category id list.
     *
     * @param categoryIdList category id list must not be empty
     * @return a list of post category
     */
    @Query("select pc from PostCategory pc where pc.categoryId in (?1)")
    @NonNull
    List<PostCategory> findAllByCategoryIdList(List<Integer> categoryIdList);

    /**
     * Finds all category ids with post id and status.
     *
     * @return a list of category id and post status
     */
    @NonNull
    @Query("select new run.halo.app.model.projection.CategoryIdPostStatusProjection(pc.categoryId,"
        + " pc.postId, p.status) from PostCategory pc inner join Post p on p.id=pc.postId")
    List<CategoryIdPostStatusProjection> findAllWithPostStatus();
}
