package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.model.vo.CategoryVO;
import cc.ryanc.halo.repository.CategoryRepository;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public List<CategoryVO> listAsTree(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // List all category
        List<Category> categories = listAll(sort);

        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }

        // Create top category
        CategoryVO topLevelCategory = createTopLevelCategory();

        // Concrete the tree
        concreteTree(topLevelCategory, categories);

        return topLevelCategory.getChildren();
    }

    /**
     * Concrete category tree.
     *
     * @param parentCategory parent category vo must not be null
     * @param categories     a list of category
     */
    private void concreteTree(CategoryVO parentCategory, List<Category> categories) {
        Assert.notNull(parentCategory, "Parent category must not be null");

        if (CollectionUtils.isEmpty(categories)) {
            return;
        }

        // Create children container for removing after
        List<Category> children = new LinkedList<>();

        categories.forEach(category -> {
            if (parentCategory.getId().equals(category.getParentId())) {
                // Save child category
                children.add(category);

                // Convert to child category vo
                CategoryVO child = new CategoryVO().convertFrom(category);

                // Init children if absent
                Optional.ofNullable(parentCategory.getChildren()).orElseGet(LinkedList::new).add(child);
            }
        });

        // Remove all child categories
        categories.removeAll(children);

        // Foreach children vos
        if (!CollectionUtils.isEmpty(parentCategory.getChildren())) {
            parentCategory.getChildren().forEach(childCategory -> concreteTree(childCategory, categories));
        }
    }

    /**
     * Creates a top level category.
     *
     * @return top level category with id 0
     */
    @NonNull
    private CategoryVO createTopLevelCategory() {
        CategoryVO topCategory = new CategoryVO();
        // Set default value
        topCategory.setId(0);
        topCategory.setChildren(new LinkedList<>());
        topCategory.setParentId(-1);

        return topCategory;
    }
}
