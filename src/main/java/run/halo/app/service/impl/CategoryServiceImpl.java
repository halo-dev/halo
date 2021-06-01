package run.halo.app.service.impl;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.UnsupportedException;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.repository.CategoryRepository;
import run.halo.app.service.AuthenticationService;
import run.halo.app.service.AuthorizationService;
import run.halo.app.service.CategoryService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.PostService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.BeanUtils;
import run.halo.app.utils.ServiceUtils;

/**
 * CategoryService implementation class.
 *
 * @author ryanwang
 * @author johnniang
 * @date 2019-03-14
 */
@Slf4j
@Service
public class CategoryServiceImpl extends AbstractCrudService<Category, Integer>
    implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final PostCategoryService postCategoryService;

    private final OptionService optionService;

    private final AuthorizationService authorizationService;

    private PostService postService;

    private final AuthenticationService authenticationService;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
        PostCategoryService postCategoryService,
        OptionService optionService,
        AuthenticationService authenticationService,
        AuthorizationService authorizationService) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
        this.postCategoryService = postCategoryService;
        this.optionService = optionService;
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
    }

    @Lazy
    @Autowired
    public void setPostService(PostService postService) {
        this.postService = postService;
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

        if (StrUtil.isNotBlank(category.getPassword())) {
            category.setPassword(category.getPassword().trim());
        }

        // Create it
        return super.create(category);
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
        concreteTree(topLevelCategory, categories, false);

        return topLevelCategory.getChildren();
    }

    /**
     * Concrete category tree.
     *
     * @param parentCategory parent category vo must not be null
     * @param categories a list of category
     * @param fillPassword whether to fill in the password
     */
    private void concreteTree(
        CategoryVO parentCategory,
        List<Category> categories,
        Boolean fillPassword
    ) {
        Assert.notNull(parentCategory, "Parent category must not be null");

        if (CollectionUtils.isEmpty(categories)) {
            return;
        }

        // Get children for removing after
        List<Category> children = categories.stream()
            .filter(category -> Objects.equal(parentCategory.getId(), category.getParentId()))
            .collect(Collectors.toList());

        children.forEach(category -> {
            // Convert to child category vo
            CategoryVO child = new CategoryVO().convertFrom(category);
            // Init children if absent
            if (parentCategory.getChildren() == null) {
                parentCategory.setChildren(new LinkedList<>());
            }

            StringBuilder fullPath = new StringBuilder();

            if (optionService.isEnabledAbsolutePath()) {
                fullPath.append(optionService.getBlogBaseUrl());
            }

            fullPath.append(URL_SEPARATOR)
                .append(optionService.getCategoriesPrefix())
                .append(URL_SEPARATOR)
                .append(child.getSlug())
                .append(optionService.getPathSuffix());

            child.setFullPath(fullPath.toString());

            if (!fillPassword) {
                child.setPassword(null);
            }

            // Add child
            parentCategory.getChildren().add(child);
        });

        // Remove all child categories
        categories.removeAll(children);

        // Foreach children vos
        if (!CollectionUtils.isEmpty(parentCategory.getChildren())) {
            parentCategory.getChildren()
                .forEach(childCategory -> concreteTree(childCategory, categories, fillPassword));
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

    @Override
    public Category getBySlug(String slug) {
        Optional<Category> bySlug = categoryRepository.getBySlug(slug);
        if (bySlug.isEmpty()) {
            return null;
        }

        Category category = bySlug.get();

        if (authenticationService.categoryAuthentication(category.getId(), null)) {
            return category;
        }

        return null;
    }

    @Override
    public Category getBySlugOfNonNull(String slug) {

        Category category = categoryRepository
            .getBySlug(slug)
            .orElseThrow(() -> new NotFoundException("查询不到该分类的信息").setErrorData(slug));

        if (authenticationService.categoryAuthentication(category.getId(), null)) {
            return category;
        }

        throw new NotFoundException("查询不到该分类的信息").setErrorData(slug);
    }

    @Override
    public Category getBySlugOfNonNull(String slug, boolean queryEncryptCategory) {
        if (queryEncryptCategory) {
            return categoryRepository.getBySlug(slug)
                .orElseThrow(() -> new NotFoundException("查询不到该分类的信息").setErrorData(slug));
        } else {
            return this.getBySlugOfNonNull(slug);
        }
    }

    @Override
    public Category getByName(String name) {
        Optional<Category> byName = categoryRepository.getByName(name);
        if (byName.isEmpty()) {
            return null;
        }

        Category category = byName.get();

        if (authenticationService.categoryAuthentication(category.getId(), null)) {
            return category;
        }

        return null;
    }

    @Override
    @Transactional
    public void removeCategoryAndPostCategoryBy(Integer categoryId) {
        List<Category> categories = listByParentId(categoryId);
        if (null != categories && categories.size() > 0) {
            categories.forEach(category -> {
                category.setParentId(0);
                update(category);
            });
        }

        // Remove category
        removeById(categoryId);
        // Remove post categories
        List<Integer> affectedPostIdList = postCategoryService.removeByCategoryId(categoryId)
            .stream().map(PostCategory::getPostId).collect(Collectors.toList());

        refreshPostStatus(affectedPostIdList);
    }

    @Override
    public void refreshPostStatus(List<Integer> affectedPostIdList) {
        if (CollectionUtil.isEmpty(affectedPostIdList)) {
            return;
        }

        for (Integer postId : affectedPostIdList) {
            Post post = postService.getById(postId);

            post.setStatus(null);

            if (StrUtil.isNotBlank(post.getPassword())) {
                post.setStatus(PostStatus.INTIMATE);
            } else {
                postCategoryService.listByPostId(postId)
                    .stream().map(PostCategory::getCategoryId)
                    .filter(this::categoryHasEncrypt)
                    .findAny()
                    .ifPresent(id -> post.setStatus(PostStatus.INTIMATE));
            }

            if (post.getStatus() == null) {
                post.setStatus(PostStatus.PUBLISHED);
            }

            postService.update(post);
        }

    }

    @Override
    public List<Category> listByParentId(Integer id) {
        Assert.notNull(id, "Parent id must not be null");
        return categoryRepository.findByParentId(id);
    }

    @Override
    public CategoryDTO convertTo(Category category) {
        Assert.notNull(category, "Category must not be null");

        CategoryDTO categoryDTO = new CategoryDTO().convertFrom(category);

        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()) {
            fullPath.append(optionService.getBlogBaseUrl());
        }

        fullPath.append(URL_SEPARATOR)
            .append(optionService.getCategoriesPrefix())
            .append(URL_SEPARATOR)
            .append(category.getSlug())
            .append(optionService.getPathSuffix());

        categoryDTO.setFullPath(fullPath.toString());

        return categoryDTO;
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
    public List<Category> filterEncryptCategory(List<Category> categories) {
        if (CollectionUtil.isEmpty(categories)) {
            return Collections.emptyList();
        }

        // Concrete the tree
        CategoryVO topLevelCategory = createTopLevelCategory();

        concreteTree(topLevelCategory, categories, true);

        List<CategoryVO> childrenList = topLevelCategory.getChildren();

        // filter encrypt category
        doFilterEncryptCategory(childrenList);

        List<Category> collectorList = new ArrayList<>();

        collectAllChild(collectorList, childrenList, true);

        for (Category category : collectorList) {
            category.setPassword(null);
        }

        return collectorList;
    }

    /**
     * do filter encrypt category
     *
     * @param categoryList category list
     */
    private void doFilterEncryptCategory(List<CategoryVO> categoryList) {
        if (CollectionUtil.isEmpty(categoryList)) {
            return;
        }

        for (CategoryVO categoryVO : categoryList) {
            if (!authenticationService.categoryAuthentication(categoryVO.getId(), null)) {
                // if parent category is not certified, the child category is not displayed.
                categoryVO.setChildren(null);
            } else {
                doFilterEncryptCategory(categoryVO.getChildren());
            }
        }
    }

    /**
     * Collect all child from tree
     *
     * @param collectorList collector
     * @param childrenList contains categories of children
     * @param doNotCollectEncryptedCategory true is not collect, false is collect
     */
    private void collectAllChild(List<Category> collectorList,
        List<CategoryVO> childrenList,
        Boolean doNotCollectEncryptedCategory) {
        if (CollectionUtil.isEmpty(childrenList)) {
            return;
        }

        for (CategoryVO categoryVO : childrenList) {

            Category category = new Category();
            BeanUtils.updateProperties(categoryVO, category);

            collectorList.add(category);

            if (doNotCollectEncryptedCategory
                && !authenticationService.categoryAuthentication(category.getId(), null)) {
                continue;
            }

            if (CollectionUtil.isNotEmpty(categoryVO.getChildren())) {
                collectAllChild(collectorList,
                    categoryVO.getChildren(), doNotCollectEncryptedCategory);
            }

        }
    }

    /**
     * Collect sub-categories under the specified category.
     *
     * @param collectorList collector
     * @param childrenList contains categories of children
     * @param categoryId category id
     * @param doNotCollectEncryptedCategory true is not collect, false is collect
     */
    private void collectAllChildByCategoryId(List<Category> collectorList,
        List<CategoryVO> childrenList,
        Integer categoryId,
        Boolean doNotCollectEncryptedCategory) {
        if (CollectionUtil.isEmpty(childrenList)) {
            return;
        }

        for (CategoryVO categoryVO : childrenList) {
            if (categoryVO.getId().equals(categoryId)) {
                collectAllChild(collectorList,
                    categoryVO.getChildren(), doNotCollectEncryptedCategory);
                break;
            }
        }
    }

    @Override
    public List<Category> listAll(Sort sort, boolean queryEncryptCategory) {
        if (queryEncryptCategory) {
            return super.listAll(sort);
        } else {
            return this.listAll(sort);
        }
    }

    @Override
    public List<Category> listAll(boolean queryEncryptCategory) {
        if (queryEncryptCategory) {
            return super.listAll();
        } else {
            return this.listAll();
        }
    }

    @Override
    public List<Category> listAll() {
        return filterEncryptCategory(super.listAll());
    }

    @Override
    public List<Category> listAll(Sort sort) {
        return filterEncryptCategory(super.listAll(sort));
    }

    @Override
    public Page<Category> listAll(Pageable pageable) {
        // To prevent developers from querying encrypted categories,
        // so paging query operations are not supported here. If you
        // really need to use this method, refactor this method to do memory paging.
        throw new UnsupportedException("Does not support business layer paging query.");
    }

    @Override
    public List<Category> listAllByIds(Collection<Integer> integers, boolean queryEncryptCategory) {
        if (queryEncryptCategory) {
            return super.listAllByIds(integers);
        } else {
            return this.listAllByIds(integers);
        }
    }

    @Override
    public List<Category> listAllByIds(Collection<Integer> integers) {
        return filterEncryptCategory(super.listAllByIds(integers));
    }

    @Override
    public List<Category> listAllByIds(Collection<Integer> integers, Sort sort) {
        return filterEncryptCategory(super.listAllByIds(integers, sort));
    }

    @Override
    @Transactional
    public Category update(Category category) {
        Category update = super.update(category);

        if (StrUtil.isNotBlank(category.getPassword())) {
            doEncryptPost(category);
        } else {
            doDecryptPost(category);
        }

        // Remove authorization every time an category is updated.
        authorizationService.deleteCategoryAuthorization(category.getId());

        return update;
    }

    /**
     * Encrypting a category requires encrypting all articles under the category
     *
     * @param category need encrypt category
     */
    private void doEncryptPost(Category category) {
        CategoryVO topLevelCategory = createTopLevelCategory();

        concreteTree(topLevelCategory, super.listAll(), true);

        List<Category> collectorList = new ArrayList<>();

        collectAllChildByCategoryId(collectorList,
            topLevelCategory.getChildren(), category.getId(), true);

        Optional.of(collectorList.stream().map(Category::getId).collect(Collectors.toList()))
            .map(categoryIdList -> {
                categoryIdList.add(category.getId());
                return categoryIdList;
            })
            .map(postCategoryService::listByCategoryIdList)

            .filter(postCategoryList -> !postCategoryList.isEmpty())
            .map(postCategoryList -> postCategoryList
                .stream().map(PostCategory::getPostId).distinct().collect(Collectors.toList()))

            .filter(postIdList -> !postIdList.isEmpty())
            .map(postIdList -> postService.listAllByIds(postIdList))

            .filter(postList -> !postList.isEmpty())
            .map(postList -> postList.stream()
                .filter(post -> PostStatus.PUBLISHED.equals(post.getStatus()))
                .map(Post::getId).collect(Collectors.toList()))

            .filter(postIdList -> !postIdList.isEmpty())
            .map(postIdList -> postService.updateStatusByIds(postIdList, PostStatus.INTIMATE));
    }

    private void doDecryptPost(Category category) {

        List<Category> allCategoryList = super.listAll();

        Map<Integer, Category> idToCategoryMap = allCategoryList.stream().collect(
            Collectors.toMap(Category::getId, Function.identity()));

        if (doCategoryHasEncrypt(idToCategoryMap, category.getParentId())) {
            // If the parent category is encrypted, there is no need to update the encryption status
            return;
        }

        CategoryVO topLevelCategory = createTopLevelCategory();

        concreteTree(topLevelCategory, allCategoryList, true);

        List<Category> collectorList = new ArrayList<>();

        // Only collect unencrypted sub-categories under the category.
        collectAllChildByCategoryId(collectorList,
            topLevelCategory.getChildren(), category.getId(), false);
        // Collect the currently decrypted category
        collectorList.add(category);

        Optional.of(collectorList.stream().map(Category::getId).collect(Collectors.toList()))
            .map(postCategoryService::listByCategoryIdList)

            .filter(postCategoryList -> !postCategoryList.isEmpty())
            .map(postCategoryList -> postCategoryList
                .stream().map(PostCategory::getPostId).distinct().collect(Collectors.toList()))

            .filter(postIdList -> !postIdList.isEmpty())
            .map(postIdList -> postService.listAllByIds(postIdList))

            .filter(postList -> !postList.isEmpty())
            .map(postList -> postList.stream()
                .filter(post -> StrUtil.isBlank(post.getPassword()))
                .filter(post -> PostStatus.INTIMATE.equals(post.getStatus()))
                .map(Post::getId).collect(Collectors.toList()))

            .filter(postIdList -> !postIdList.isEmpty())
            .map(postIdList -> postService.updateStatusByIds(postIdList, PostStatus.PUBLISHED));
    }

    @Override
    public Boolean categoryHasEncrypt(Integer categoryId) {
        List<Category> allCategoryList = super.listAll();

        Map<Integer, Category> idToCategoryMap = allCategoryList.stream().collect(
            Collectors.toMap(Category::getId, Function.identity()));

        return doCategoryHasEncrypt(idToCategoryMap, categoryId);
    }

    /**
     * Find whether the parent category is encrypted.
     *
     * @param idToCategoryMap find category by id
     * @param categoryId category id
     * @return whether to encrypt
     */
    private boolean doCategoryHasEncrypt(
        Map<Integer, Category> idToCategoryMap, Integer categoryId) {

        if (categoryId == 0) {
            return false;
        }

        Category category = idToCategoryMap.get(categoryId);

        if (StrUtil.isNotBlank(category.getPassword())) {
            return true;
        }

        return doCategoryHasEncrypt(idToCategoryMap, category.getParentId());
    }


    @Override
    public List<Category> updateInBatch(Collection<Category> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }

        ArrayList<Category> resultList = new ArrayList<>();
        for (Category category : categories) {
            resultList.add(update(category));
        }
        return resultList;
    }
}
