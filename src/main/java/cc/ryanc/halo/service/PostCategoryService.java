package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.entity.PostCategory;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Post category service interface.
 *
 * @author johnniang
 * @date 3/19/19
 */
public interface PostCategoryService extends CrudService<PostCategory, Integer> {

    /**
     * Lists category by post id.
     *
     * @param postId post id must not be null
     * @return a list of category
     */
    @NonNull
    List<Category> listCategoryBy(@NonNull Integer postId);

    /**
     * List category list map by post id collection.
     *
     * @param postIds post id collection
     * @return a category list map (key: postId, value: a list of category)
     */
    @NonNull
    Map<Integer, List<Category>> listCategoryListMap(@Nullable Collection<Integer> postIds);

    /**
     * Lists post by category id.
     *
     * @param categoryId category id must not be null
     * @return a list of post
     */
    @NonNull
    List<Post> listPostBy(@NonNull Integer categoryId);

    /**
     * Merges or creates post categories by post id and category id set if absent.
     *
     * @param postId      post id must not be null
     * @param categoryIds category id set
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> mergeOrCreateByIfAbsent(@NonNull Integer postId, @Nullable Set<Integer> categoryIds);

    /**
     * Lists by post id.
     *
     * @param postId post id must not be null
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> listByPostId(@NonNull Integer postId);

    /**
     * Lists by category id.
     *
     * @param categoryId category id must not be null
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> listByCategoryId(@NonNull Integer categoryId);

    /**
     * List category id set by post id.
     *
     * @param postId post id must not be null
     * @return a set of category id
     */
    @NonNull
    Set<Integer> listCategoryIdsByPostId(@NonNull Integer postId);

    /**
     * Removes post categories by post id.
     *
     * @param postId post id must not be null
     * @return a list of post category deleted
     */
    @NonNull
    List<PostCategory> removeByPostId(@NonNull Integer postId);

    /**
     * Removes post categories by category id.
     *
     * @param categoryId category id must not be null
     * @return a list of post category deleted
     */
    @NonNull
    List<PostCategory> removeByCategoryId(@NonNull Integer categoryId);
}
