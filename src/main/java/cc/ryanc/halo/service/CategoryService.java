package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.service.base.CrudService;

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
    void remove(Integer id);
}
