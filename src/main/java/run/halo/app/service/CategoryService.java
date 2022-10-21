package run.halo.app.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
 * @author guqing
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
     * Build category full path.
     *
     * @param slug category slug name.
     * @return full path of category.
     */
    @NonNull
    String buildCategoryFullPath(@NonNull String slug);

    /**
     * Get category by slug.
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
     * List categories by parent id.
     *
     * @param id parent id.
     * @return list of category.
     */
    List<Category> listByParentId(@NonNull Integer id);

    /**
     * List all child categories and current category by parent id.
     *
     * @param id parent id.
     * @return a list of category that contain current id.
     */
    List<Category> listAllByParentId(@NonNull Integer id);

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
     * Determine whether the category is encrypted.
     *
     * @param categoryId category id
     * @return whether to encrypt
     */
    boolean isPrivate(Integer categoryId);

    /**
     * This method will first query all categories and create a tree, then start from the node
     * whose ID is <code>categoryId</code> and recursively look up the first encryption category.
     *
     * @param categoryId categoryId to look up
     * @return encrypted immediate parent category If it is found,otherwise an empty
     * {@code Optional}.
     */
    Optional<Category> lookupFirstEncryptedBy(Integer categoryId);

    /**
     * Use <code>categories</code> to build a category tree.
     *
     * @param categories categories to build a tree.
     * @return a tree of category.
     */
    List<CategoryVO> listToTree(List<Category> categories);

    /**
     * Recursively query the associated post ids according to the category id.
     *
     * @param categoryId category id
     * @return a collection of post ids
     */
    @NonNull
    Set<Integer> listPostIdsByCategoryIdRecursively(@NonNull Integer categoryId);
}
