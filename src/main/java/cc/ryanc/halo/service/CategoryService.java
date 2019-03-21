package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.model.vo.CategoryVO;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

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
}
