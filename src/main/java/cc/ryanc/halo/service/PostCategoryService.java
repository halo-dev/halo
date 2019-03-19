package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.entity.PostCategory;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    Map<Integer, List<Category>> listCategoryListMap(Collection<Integer> postIds);

    /**
     * Lists post by category id.
     *
     * @param categoryId category id must not be null
     * @return a list of post
     */
    @NonNull
    List<Post> listPostBy(@NonNull Integer categoryId);
}
