package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.projection.CategoryPostCountProjection;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;
import java.util.Set;


/**
 * Post category repository.
 *
 * @author johnniang
 */
public interface PostCategoryRepository extends BaseRepository<PostCategory, Integer> {

    /**
     * Finds all category ids by post id
     *
     * @param postId post id must not be null
     * @return a list of category id
     */
    @NonNull
    @Query("select postCategory.categoryId from PostCategory postCategory where postCategory.postId = ?1")
    Set<Integer> findAllCategoryIdsByPostId(@NonNull Integer postId);

    /**
     * Finds all post ids by category id.
     *
     * @param categoryId category id must not be null
     * @return a set of post id
     */
    @NonNull
    @Query("select postCategory.postId from PostCategory postCategory where postCategory.categoryId = ?1")
    Set<Integer> findAllPostIdsByCategoryId(@NonNull Integer categoryId);

    /**
     * Finds all post categories by post id in.
     *
     * @param postIds post id collection must not be null
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> findAllByPostIdIn(@NonNull Iterable<Integer> postIds);

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

    @Query("select new run.halo.app.model.projection.CategoryPostCountProjection(count(pc.postId), pc.categoryId) from PostCategory pc group by pc.categoryId")
    @NonNull
    List<CategoryPostCountProjection> findPostCount();
}
