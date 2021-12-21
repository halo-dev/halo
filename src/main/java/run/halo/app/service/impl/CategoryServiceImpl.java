package run.halo.app.service.impl;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import com.google.common.base.Objects;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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

        if (StringUtils.isNotBlank(category.getPassword())) {
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

    @NonNull
    private CategoryVO convertToCategoryVo(Category category) {
        Assert.notNull(category, "The category must not be null.");
        CategoryVO categoryVo = new CategoryVO().convertFrom(category);
        categoryVo.setFullPath(buildCategoryFullPath(categoryVo.getSlug()));
        return categoryVo;
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
        if (CollectionUtils.isEmpty(affectedPostIdList)) {
            return;
        }

        for (Integer postId : affectedPostIdList) {
            Post post = postService.getById(postId);

            post.setStatus(null);

            if (StringUtils.isNotBlank(post.getPassword())) {
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
            if (Objects.equal(category.getId(), categoryId)) {
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
    public List<Category> filterEncryptCategory(List<Category> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }

        // list to tree, no password desensitise is required here
        List<CategoryVO> categoryTree = listToTree(categories);

        // filter encrypt category
        doFilterEncryptCategory(categoryTree);

        List<Category> collectorList = new ArrayList<>();

        collectAllChild(collectorList, categoryTree, true);

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
        if (CollectionUtils.isEmpty(categoryList)) {
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
        if (CollectionUtils.isEmpty(childrenList)) {
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

            if (HaloUtils.isNotEmpty(categoryVO.getChildren())) {
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
        if (CollectionUtils.isEmpty(childrenList)) {
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

        if (StringUtils.isNotBlank(category.getPassword())) {
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
        // list to tree with password
        List<CategoryVO> categoryTree = listToTree(super.listAll());

        List<Category> collectorList = new ArrayList<>();

        collectAllChildByCategoryId(collectorList,
            categoryTree, category.getId(), true);

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
        // with password
        List<CategoryVO> categoryTree = listToTree(allCategoryList);

        List<Category> collectorList = new ArrayList<>();

        // Only collect unencrypted sub-categories under the category.
        collectAllChildByCategoryId(collectorList,
            categoryTree, category.getId(), false);
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
                .filter(post -> StringUtils.isBlank(post.getPassword()))
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

        if (StringUtils.isNotBlank(category.getPassword())) {
            return true;
        }

        return doCategoryHasEncrypt(idToCategoryMap, category.getParentId());
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
                Category categoryParam = idCategoryParamMap.get(categoryToUpdate.getId());
                BeanUtils.updateProperties(categoryParam, categoryToUpdate);
                return update(categoryToUpdate);
            })
            .collect(Collectors.toList());
    }
}
