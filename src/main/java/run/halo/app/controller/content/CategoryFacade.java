package run.halo.app.controller.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.controller.content.auth.CategoryAuthentication;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.service.CategoryService;

/**
 * @author guqing
 * @date 2022-02-23
 */
@Component
public class CategoryFacade {
    private final CategoryService categoryService;
    private final CategoryAuthentication categoryAuthentication;

    public CategoryFacade(CategoryService categoryService,
        CategoryAuthentication categoryAuthentication) {
        this.categoryService = categoryService;
        this.categoryAuthentication = categoryAuthentication;
    }

    public Category getBySlugOfNonNull(String slug) {
        return categoryService.getBySlugOfNonNull(slug);
    }

    public List<Category> listAll() {
        List<Category> categories = categoryService.listAll();
        return filterEncryptedCategory(categories);
    }

    public List<Category> listAll(Sort sort) {
        List<Category> categories = categoryService.listAll(sort);
        return filterEncryptedCategory(categories);
    }

    public CategoryDTO convertTo(Category category) {
        return categoryService.convertTo(category);
    }

    public List<CategoryDTO> convertTo(List<Category> categories) {
        return categoryService.convertTo(categories);
    }

    public List<Category> filterEncryptedCategory(List<Category> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }
        List<Integer> categoriesToRemove = new ArrayList<>();
        // list to tree, no password desensitise is required here
        List<CategoryVO> categoryTree = categoryService.listToTree(categories);
        for (Category category : categories) {
            if (category.getPassword() == null) {
                continue;
            }
            categoryService.findCategoryTreeNodeById(categoryTree, category.getId())
                .ifPresent(categoryVO -> {
                    List<Integer> categoryIds =
                        categoryService.walkCategoryTree(categoryVO)
                            .stream()
                            .map(Category::getId)
                            // if authenticated by current session, do not remove from categories
                            .filter(
                                categoryId -> !categoryAuthentication.isAuthenticated(categoryId))
                            .collect(Collectors.toList());
                    categoriesToRemove.addAll(categoryIds);
                });
        }

        // Remove category from categories by id
        return categories.stream()
            .filter(category -> !categoriesToRemove.contains(category.getId()))
            .collect(Collectors.toList());
    }
}
