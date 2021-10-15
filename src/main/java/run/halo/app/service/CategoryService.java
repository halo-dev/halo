package run.halo.app.service;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.service.base.CrudService;

/**
 * Category service.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-14
 */
@Transactional(readOnly = true)
public interface CategoryService extends CrudService<Category, Integer> {

    /**
     * Lists as category tree.
     *
     * @param sort sort info must not be null
     * @return a category tree
     */
    @NonNull
    List<CategoryVO> listAsTree(@NonNull Sort sort);

    /**
     * Get category by slug
     *
     * @param slug slug
     * @return Category
     */
    Category getBySlug(@NonNull String slug);

    /**
     * Get category by slug
     *
     * @param slug slug
     * @return Category
     */
    @NonNull
    Category getBySlugOfNonNull(String slug);

    /**
     * Get category by slug
     *
     * @param slug slug
     * @param queryEncryptCategory whether to query encryption category
     * @return Category
     */
    @NonNull
    Category getBySlugOfNonNull(String slug, boolean queryEncryptCategory);

    /**
     * Get Category by name.
     *
     * @param name name
     * @return Category
     */
    @Nullable
    Category getByName(@NonNull String name);

    /**
     * Removes category and post categories.
     *
     * @param categoryId category id must not be null
     */
    @Transactional
    void removeCategoryAndPostCategoryBy(Integer categoryId);

    /**
     * Refresh post status, when the post is under the encryption category or is has a password,
     * it is changed to private, otherwise it is changed to public.
     *
     * @param affectedPostIdList affected post id list
     */
    void refreshPostStatus(List<Integer> affectedPostIdList);

    /**
     * List categories by parent id.
     *
     * @param id parent id.
     * @return list of category.
     */
    List<Category> listByParentId(@NonNull Integer id);

    /**
     * List all category not encrypt.
     *
     * @param sort sort
     * @param queryEncryptCategory whether to query encryption category
     * @return list of category.
     */
    @NonNull
    List<Category> listAll(Sort sort, boolean queryEncryptCategory);

    /**
     * List all category not encrypt.
     *
     * @param queryEncryptCategory whether to query encryption category
     * @return list of category.
     */
    List<Category> listAll(boolean queryEncryptCategory);

    /**
     * List all by ids
     *
     * @param ids ids
     * @param queryEncryptCategory whether to query encryption category
     * @return List
     */
    @NonNull
    List<Category> listAllByIds(Collection<Integer> ids, boolean queryEncryptCategory);

    /**
     * Converts to category dto.
     *
     * @param category category must not be null
     * @return category dto
     */
    @NonNull
    CategoryDTO convertTo(@NonNull Category category);

    /**
     * Converts to category dto list.
     *
     * @param categories category list
     * @return a list of category dto
     */
    @NonNull
    List<CategoryDTO> convertTo(@Nullable List<Category> categories);

    /**
     * Filter encrypt category
     *
     * @param categories this categories is not a category list tree
     * @return category list
     */
    @NonNull
    List<Category> filterEncryptCategory(@Nullable List<Category> categories);

    /**
     * Determine whether the category is encrypted.
     *
     * @param categoryId category id
     * @return whether to encrypt
     */
    @NonNull
    Boolean categoryHasEncrypt(Integer categoryId);
}
