package run.halo.app.service.impl;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.category.CategoryUpdatedEvent;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.repository.CategoryRepository;
import run.halo.app.service.CategoryService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.BeanUtils;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.ServiceUtils;

/**
 * CategoryService implementation class.
 *
 * @author ryanwang
 * @author johnniang
 * @author guqing
 * @date 2019-03-14
 */
@Slf4j
@Service
public class CategoryServiceImpl extends AbstractCrudService<Category, Integer>
    implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final PostCategoryService postCategoryService;

    private final OptionService optionService;

    private final ApplicationContext applicationContext;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
        PostCategoryService postCategoryService,
        OptionService optionService,
        ApplicationContext applicationContext) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
        this.postCategoryService = postCategoryService;
        this.optionService = optionService;
        this.applicationContext = applicationContext;
    }

    @Override
    @Transactional
    public Category create(Category category) {
        Assert.notNull(category, "Category to create must not be null");

        // Check the category name
        long count = categoryRepository.countByName(category.getName());

        if (count > 0) {
            log.error("Category has exist already: [{}]", category);
            throw new AlreadyExistsException("该分类已存在");
        }

        // Check parent id
        if (!ServiceUtils.isEmptyId(category.getParentId())) {
            count = categoryRepository.countById(category.getParentId());

            if (count == 0) {
                log.error("Parent category with id: [{}] was not found, category: [{}]",
                    category.getParentId(), category);
                throw new NotFoundException(
                    "Parent category with id = " + category.getParentId() + " was not found");
            }
        }

        if (StringUtils.isNotBlank(category.getPassword())) {
            category.setPassword(category.getPassword().trim());
        }

        // Create it
        return super.create(category);
    }

    @Override
    public Category update(Category category) {
        Category persisted = getById(category.getId());
        Category beforeUpdated = new Category();
        BeanUtils.updateProperties(persisted, beforeUpdated);
        boolean beforeIsPrivate = isPrivate(category.getId());

        Category updated = super.update(category);

        Set<Integer> postIds = listPostIdsByCategoryIdRecursively(category.getId());
        applicationContext.publishEvent(
            new CategoryUpdatedEvent(this, category, beforeUpdated, beforeIsPrivate, postIds));
        return updated;
    }

    @Override
    public List<CategoryVO> listAsTree(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // List all category
        List<Category> categories = listAll(sort);

        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }
        // Do not return category password.
        desensitizePassword(categories);

        return listToTree(categories);
    }

    private void desensitizePassword(List<Category> categories) {
        Assert.notNull(categories, "The categories must not be null.");
        categories.forEach(category -> {
            category.setPassword(null);
        });
    }

    @NonNull
    @Override
    public String buildCategoryFullPath(@NonNull String slug) {
        Assert.notNull(slug, "The slug must not be null.");
        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()) {
            fullPath.append(optionService.getBlogBaseUrl());
        }

        fullPath.append(URL_SEPARATOR)
            .append(optionService.getCategoriesPrefix())
            .append(URL_SEPARATOR)
            .append(slug)
            .append(optionService.getPathSuffix());
        return fullPath.toString();
    }

    @Override
    public Category getBySlug(String slug) {
        return categoryRepository.getBySlug(slug).orElse(null);
    }

    @NonNull
    private CategoryVO convertToCategoryVo(Category category) {
        Assert.notNull(category, "The category must not be null.");
        CategoryVO categoryVo = new CategoryVO().convertFrom(category);
        categoryVo.setFullPath(buildCategoryFullPath(categoryVo.getSlug()));
        return categoryVo;
    }

    @Override
    public Category getBySlugOfNonNull(String slug) {
        return categoryRepository
            .getBySlug(slug)
            .orElseThrow(() -> new NotFoundException("查询不到该分类的信息").setErrorData(slug));
    }

    @Override
    public Category getByName(String name) {
        return categoryRepository.getByName(name).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCategoryAndPostCategoryBy(Integer categoryId) {
        final boolean beforeIsPrivate = isPrivate(categoryId);
        final Set<Integer> postIds = listPostIdsByCategoryIdRecursively(categoryId);
        List<Category> categories = listByParentId(categoryId);
        if (null != categories && categories.size() > 0) {
            categories.forEach(category -> {
                category.setParentId(0);
                update(category);
            });
        }

        // Remove category
        Category category = removeById(categoryId);
        // Remove post categories
        postCategoryService.removeByCategoryId(categoryId);

        applicationContext.publishEvent(
            new CategoryUpdatedEvent(this, null, category, beforeIsPrivate, postIds));
    }

    @Override
    public List<Category> listByParentId(@NonNull Integer id) {
        Assert.notNull(id, "Parent id must not be null");
        return categoryRepository.findByParentId(id);
    }

    @Override
    public List<Category> listAllByParentId(@NonNull Integer id) {
        Assert.notNull(id, "Parent id must not be null");
        List<Category> categories = super.listAll(Sort.by(Order.asc("name")));
        List<CategoryVO> categoryTree = listToTree(categories);
        return findCategoryTreeNodeById(categoryTree, id)
            .map(this::walkCategoryTree)
            .orElse(Collections.emptyList());
    }

    /**
     * Walk a category tree with root node.
     *
     * @param root a root node of category tree.
     * @return a flattened category list
     */
    @NonNull
    private List<Category> walkCategoryTree(CategoryVO root) {
        Assert.notNull(root, "The category 'root' must not be null");
        List<Category> categories = new LinkedList<>();
        Queue<CategoryVO> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            CategoryVO categoryNode = queue.poll();

            Category category = new Category();
            BeanUtils.updateProperties(categoryNode, category);
            categories.add(category);

            if (HaloUtils.isNotEmpty(categoryNode.getChildren())) {
                queue.addAll(categoryNode.getChildren());
            }
        }
        return categories;
    }

    /**
     * Find the node with id equal to <code>categoryId</code> from the multi-tree of category.
     *
     * @param categoryVos source category multi-tree vos to walk.
     * @param categoryId category id to be found.
     * @return the root node of the subtree.
     */
    private Optional<CategoryVO> findCategoryTreeNodeById(List<CategoryVO> categoryVos,
        Integer categoryId) {
        Assert.notNull(categoryId, "categoryId id must not be null");
        Queue<CategoryVO> queue = new ArrayDeque<>(categoryVos);
        while (!queue.isEmpty()) {
            CategoryVO category = queue.poll();
            if (Objects.equals(category.getId(), categoryId)) {
                return Optional.of(category);
            }
            if (HaloUtils.isNotEmpty(category.getChildren())) {
                queue.addAll(category.getChildren());
            }
        }
        return Optional.empty();
    }

    @Override
    public CategoryDTO convertTo(Category category) {
        Assert.notNull(category, "Category must not be null");

        CategoryDTO categoryDto = new CategoryDTO().convertFrom(category);

        categoryDto.setFullPath(buildCategoryFullPath(category.getSlug()));

        return categoryDto;
    }

    @Override
    public List<CategoryDTO> convertTo(List<Category> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }

        return categories.stream()
            .map(this::convertTo)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isPrivate(Integer categoryId) {
        return lookupFirstEncryptedBy(categoryId).isPresent();
    }

    @Override
    public List<CategoryVO> listToTree(List<Category> categories) {
        Assert.notNull(categories, "The categories must not be null.");
        // batch convert category to categoryVo
        List<CategoryVO> categoryVoList = categories.stream()
            .map(this::convertToCategoryVo)
            .collect(Collectors.toList());

        // build a tree, the time complexity is O(n)
        Map<Integer, List<CategoryVO>> parentIdMap = categoryVoList.stream()
            .collect(Collectors.groupingBy(CategoryVO::getParentId));

        // set children
        categoryVoList.forEach(category -> {
            List<CategoryVO> children = parentIdMap.get(category.getId());
            if (CollectionUtils.isEmpty(children)) {
                category.setChildren(Collections.emptyList());
            } else {
                category.setChildren(children);
            }
        });

        return categoryVoList.stream()
            .filter(category -> category.getParentId() == null || category.getParentId() == 0)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Category> lookupFirstEncryptedBy(Integer categoryId) {
        List<Category> categories = listAll();
        Map<Integer, Category> categoryMap =
            ServiceUtils.convertToMap(categories, Category::getId);
        return Optional.ofNullable(findFirstEncryptedCategoryBy(categoryMap, categoryId));
    }

    /**
     * Find whether the parent category is encrypted.
     * If it is found, the result will be returned immediately.
     * Otherwise, it will be found recursively according to parentId.
     *
     * @param idToCategoryMap find category by id
     * @param categoryId category id
     * @return whether to encrypt
     */
    private Category findFirstEncryptedCategoryBy(Map<Integer, Category> idToCategoryMap,
        Integer categoryId) {
        Category category = idToCategoryMap.get(categoryId);

        if (categoryId == 0 || category == null) {
            return null;
        }

        if (StringUtils.isNotBlank(category.getPassword())) {
            return category;
        }

        return findFirstEncryptedCategoryBy(idToCategoryMap, category.getParentId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Category> updateInBatch(Collection<Category> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }
        Set<Integer> categoryIds = ServiceUtils.fetchProperty(categories, Category::getId);
        Map<Integer, Category> idCategoryParamMap =
            ServiceUtils.convertToMap(categories, Category::getId);
        return categoryRepository.findAllById(categoryIds)
            .stream()
            .map(categoryToUpdate -> {
                // 将持久化状态的对象转非session管理对象否则数据会被更新
                Category categoryBefore = BeanUtils.transformFrom(categoryToUpdate, Category.class);
                boolean beforeIsPrivate = isPrivate(categoryToUpdate.getId());

                Category categoryParam = idCategoryParamMap.get(categoryToUpdate.getId());
                BeanUtils.updateProperties(categoryParam, categoryToUpdate);
                Category categoryUpdated = update(categoryToUpdate);

                Set<Integer> postIds = listPostIdsByCategoryIdRecursively(categoryUpdated.getId());
                applicationContext.publishEvent(new CategoryUpdatedEvent(this,
                    categoryUpdated, categoryBefore, beforeIsPrivate, postIds));
                return categoryUpdated;
            })
            .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public Set<Integer> listPostIdsByCategoryIdRecursively(@NonNull Integer categoryId) {
        Set<Integer> categoryIds = ServiceUtils.fetchProperty(listAllByParentId(categoryId),
            Category::getId);
        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptySet();
        }
        List<PostCategory> postCategories =
            postCategoryService.listByCategoryIdList(new ArrayList<>(categoryIds));
        return ServiceUtils.fetchProperty(postCategories, PostCategory::getPostId);
    }

}
