package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.repository.CategoryRepository;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * CategoryService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class CategoryServiceImpl extends AbstractCrudService<Category, Integer> implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
    }

    /**
     * Remove category and relationship
     *
     * @param id id
     */
    @Override
    public void remove(Integer id) {
        // TODO 删除分类，以及和文章的对应关系
    }
}
