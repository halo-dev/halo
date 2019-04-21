package run.halo.app.service;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.entity.Category;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.service.base.CrudService;

import java.util.List;

/**
 * Category service.
 *
 * @author johnniang
 */
public interface CategoryService extends CrudService<Category, Integer> {

    /**
     * Remove category and relationship
     *
     * @param id id
     */
    @Deprecated
    void remove(@NonNull Integer id);

    /**
     * Lists as category tree.
     *
     * @param sort sort info must not be null
     * @return a category tree
     */
    @NonNull
    List<CategoryVO> listAsTree(@NonNull Sort sort);

    /**
     * Get category by slug name
     *
     * @param slugName slug name
     * @return Category
     */
    @NonNull
    Category getBySlugName(@NonNull String slugName);

    /**
     * Removes category and post categories.
     *
     * @param categoryId category id must not be null
     */
    @Transactional
    void removeCategoryAndPostCategoryBy(Integer categoryId);
}
