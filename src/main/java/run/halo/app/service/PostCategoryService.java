package run.halo.app.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.dto.CategoryWithPostCountDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.base.CrudService;

/**
 * Post category service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-03-19
 */
public interface PostCategoryService extends CrudService<PostCategory, Integer> {

    /**
     * Lists category by post id.
     *
     * @param postId post id must not be null
     * @return a list of category
     */
    @NonNull
    List<Category> listCategoriesBy(@NonNull Integer postId);

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
     * Lists post by category id and post status.
     *
     * @param categoryId category id must not be null
     * @param status post status
     * @return a list of post
     */
    @NonNull
    List<Post> listPostBy(@NonNull Integer categoryId, @NonNull PostStatus status);

    /**
     * Lists post by category id and post status.
     *
     * @param categoryId category id must not be null
     * @param status     post status
     * @return a list of post
     */
    @NonNull
    List<Post> listPostBy(@NonNull Integer categoryId, @NonNull Set<PostStatus> status);

    /**
     * Lists post by category slug and post status.
     *
     * @param slug category slug must not be null
     * @param status post status
     * @return a list of post
     */
    @NonNull
    List<Post> listPostBy(@NonNull String slug, @NonNull PostStatus status);

    /**
     * Lists post by category slug and post status.
     *
     * @param slug   category slug must not be null
     * @param status post status
     * @return a list of post
     */
    @NonNull
    List<Post> listPostBy(@NonNull String slug, @NonNull Set<PostStatus> status);

    /**
     * Pages post by category id.
     *
     * @param categoryId category id must not be null
     * @param pageable pageable
     * @return page of post
     */
    @NonNull
    Page<Post> pagePostBy(@NonNull Integer categoryId, Pageable pageable);

    /**
     * Pages post by category id and post status.
     *
     * @param categoryId category id must not be null
     * @param status post status
     * @param pageable pageable
     * @return page of post
     */
    @NonNull
    Page<Post> pagePostBy(@NonNull Integer categoryId, @NonNull PostStatus status,
        Pageable pageable);

    /**
     * Pages post by category id and post status.
     *
     * @param categoryId category id must not be null
     * @param status     post status
     * @param pageable   pageable
     * @return page of post
     */
    @NonNull
    Page<Post> pagePostBy(
        @NonNull Integer categoryId, @NonNull Set<PostStatus> status, Pageable pageable);

    /**
     * Merges or creates post categories by post id and category id set if absent.
     *
     * @param postId post id must not be null
     * @param categoryIds category id set
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> mergeOrCreateByIfAbsent(@NonNull Integer postId,
        @Nullable Set<Integer> categoryIds);

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
    @Transactional
    List<PostCategory> removeByPostId(@NonNull Integer postId);

    /**
     * Removes post categories by category id.
     *
     * @param categoryId category id must not be null
     * @return a list of post category deleted
     */
    @NonNull
    @Transactional
    List<PostCategory> removeByCategoryId(@NonNull Integer categoryId);

    /**
     * Lists category with post count.
     *
     * @param sort sort info
     * @return a list of category dto
     */
    @NonNull
    List<CategoryWithPostCountDTO> listCategoryWithPostCountDto(@NonNull Sort sort);

    /**
     * Lists by category id.
     *
     * @param categoryIdList category id must not be empty
     * @return a list of post category
     */
    @NonNull
    List<PostCategory> listByCategoryIdList(@NonNull List<Integer> categoryIdList);
}
