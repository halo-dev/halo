package run.halo.app.service.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.CategoryWithPostCountDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.projection.CategoryIdPostStatusProjection;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.repository.PostCategoryRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.CategoryService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.ServiceUtils;

/**
 * Post category service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-03-19
 */
@Service
public class PostCategoryServiceImpl extends AbstractCrudService<PostCategory, Integer>
    implements PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;

    private final PostRepository postRepository;

    private CategoryService categoryService;

    private final OptionService optionService;

    public PostCategoryServiceImpl(PostCategoryRepository postCategoryRepository,
        PostRepository postRepository,
        OptionService optionService) {
        super(postCategoryRepository);
        this.postCategoryRepository = postCategoryRepository;
        this.postRepository = postRepository;
        this.optionService = optionService;
    }

    @Lazy
    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public List<Category> listCategoriesBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        // Find all category ids
        Set<Integer> categoryIds = postCategoryRepository.findAllCategoryIdsByPostId(postId);

        return categoryService.listAllByIds(categoryIds);
    }


    @Override
    public Map<Integer, List<Category>> listCategoryListMap(
        Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Find all post categories
        List<PostCategory> postCategories = postCategoryRepository.findAllByPostIdIn(postIds);

        // Fetch category ids
        Set<Integer> categoryIds =
            ServiceUtils.fetchProperty(postCategories, PostCategory::getCategoryId);

        // Find all categories
        List<Category> categories = categoryService.listAllByIds(categoryIds);

        // Convert to category map
        Map<Integer, Category> categoryMap = ServiceUtils.convertToMap(categories, Category::getId);

        // Create category list map
        Map<Integer, List<Category>> categoryListMap = new HashMap<>();

        // Foreach and collect
        postCategories.forEach(postCategory -> categoryListMap
            .computeIfAbsent(postCategory.getPostId(), postId -> new LinkedList<>())
            .add(categoryMap.get(postCategory.getCategoryId())));

        return categoryListMap;
    }

    @Override
    public List<Post> listPostBy(Integer categoryId) {
        Assert.notNull(categoryId, "Category id must not be null");

        // Find all post ids
        Set<Integer> postIds = postCategoryRepository.findAllPostIdsByCategoryId(categoryId);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostBy(Integer categoryId, PostStatus status) {
        Assert.notNull(categoryId, "Category id must not be null");
        Assert.notNull(status, "Post status must not be null");

        // Find all post ids
        Set<Integer> postIds =
            postCategoryRepository.findAllPostIdsByCategoryId(categoryId, status);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostBy(Integer categoryId, Set<PostStatus> status) {
        Assert.notNull(categoryId, "Category id must not be null");
        Assert.notNull(status, "Post status must not be null");

        // Find all post ids
        Set<Integer> postIds = postCategoryRepository
            .findAllPostIdsByCategoryId(categoryId, status);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostBy(String slug, Set<PostStatus> status) {
        Assert.notNull(slug, "Category slug must not be null");
        Assert.notNull(status, "Post status must not be null");

        Category category = categoryService.getBySlug(slug);

        if (Objects.isNull(category)) {
            throw new NotFoundException("查询不到该分类的信息").setErrorData(slug);
        }

        Set<Integer> postsIds = postCategoryRepository
            .findAllPostIdsByCategoryId(category.getId(), status);

        return postRepository.findAllById(postsIds);
    }

    @Override
    public List<Post> listPostBy(String slug, PostStatus status) {
        Assert.notNull(slug, "Category slug must not be null");
        Assert.notNull(status, "Post status must not be null");

        Category category = categoryService.getBySlug(slug);

        if (Objects.isNull(category)) {
            throw new NotFoundException("查询不到该分类的信息").setErrorData(slug);
        }

        Set<Integer> postsIds =
            postCategoryRepository.findAllPostIdsByCategoryId(category.getId(), status);

        return postRepository.findAllById(postsIds);
    }

    @Override
    public Page<Post> pagePostBy(Integer categoryId, Pageable pageable) {
        Assert.notNull(categoryId, "Category id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds = postCategoryRepository.findAllPostIdsByCategoryId(categoryId);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public Page<Post> pagePostBy(Integer categoryId, PostStatus status, Pageable pageable) {
        Assert.notNull(categoryId, "Category id must not be null");
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds = postCategoryRepository
            .findAllPostIdsByCategoryId(categoryId, status);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public Page<Post> pagePostBy(Integer categoryId, Set<PostStatus> status, Pageable pageable) {
        Assert.notNull(categoryId, "Category id must not be null");
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds =
            postCategoryRepository.findAllPostIdsByCategoryId(categoryId, status);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public List<PostCategory> mergeOrCreateByIfAbsent(Integer postId, Set<Integer> categoryIds) {
        Assert.notNull(postId, "Post id must not be null");

        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyList();
        }

        // Build post categories
        List<PostCategory> postCategoriesStaging = categoryIds.stream().map(categoryId -> {
            PostCategory postCategory = new PostCategory();
            postCategory.setPostId(postId);
            postCategory.setCategoryId(categoryId);
            return postCategory;
        }).collect(Collectors.toList());

        List<PostCategory> postCategoriesToCreate = new LinkedList<>();
        List<PostCategory> postCategoriesToRemove = new LinkedList<>();

        // Find all exist post categories
        List<PostCategory> postCategories = postCategoryRepository.findAllByPostId(postId);

        postCategories.forEach(postCategory -> {
            if (!postCategoriesStaging.contains(postCategory)) {
                postCategoriesToRemove.add(postCategory);
            }
        });

        postCategoriesStaging.forEach(postCategoryStaging -> {
            if (!postCategories.contains(postCategoryStaging)) {
                postCategoriesToCreate.add(postCategoryStaging);
            }
        });

        // Remove post categories
        removeAll(postCategoriesToRemove);

        // Remove all post categories need to remove
        postCategories.removeAll(postCategoriesToRemove);

        // Add all created post categories
        postCategories.addAll(createInBatch(postCategoriesToCreate));

        // Create them
        return postCategories;
    }

    @Override
    public List<PostCategory> listByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postCategoryRepository.findAllByPostId(postId);
    }

    @Override
    public List<PostCategory> listByCategoryId(Integer categoryId) {
        Assert.notNull(categoryId, "Category id must not be null");

        return postCategoryRepository.findAllByCategoryId(categoryId);
    }

    @Override
    public Set<Integer> listCategoryIdsByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postCategoryRepository.findAllCategoryIdsByPostId(postId);
    }

    @Override
    public List<PostCategory> removeByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postCategoryRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostCategory> removeByCategoryId(Integer categoryId) {
        Assert.notNull(categoryId, "Category id must not be null");

        return postCategoryRepository.deleteByCategoryId(categoryId);
    }

    @Override
    public List<CategoryWithPostCountDTO> listCategoryWithPostCountDto(@NonNull Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");
        List<Category> categories = categoryService.listAll(sort);
        List<CategoryVO> categoryTreeVo = categoryService.listToTree(categories);
        populatePostIds(categoryTreeVo, postStatus -> !PostStatus.RECYCLE.equals(postStatus));
        // Convert and return
        return flatTreeToList(categoryTreeVo);
    }

    private List<CategoryWithPostCountDTO> flatTreeToList(List<CategoryVO> categoryTree) {
        Assert.notNull(categoryTree, "The categoryTree must not be null.");
        List<CategoryWithPostCountDTO> result = new LinkedList<>();
        walkCategoryTree(categoryTree, category -> {
            CategoryWithPostCountDTO categoryWithPostCountDto =
                new CategoryWithPostCountDTO();
            BeanUtils.copyProperties(category, categoryWithPostCountDto);
            String fullPath = categoryService.buildCategoryFullPath(category.getSlug());
            categoryWithPostCountDto.setFullPath(fullPath);
            // populate post count.
            int postCount = Objects.requireNonNullElse(category.getPostIds(),
                Collections.emptySet()).size();
            categoryWithPostCountDto.setPostCount((long) postCount);
            result.add(categoryWithPostCountDto);
        });
        return result;
    }

    private void populatePostIds(List<CategoryVO> categoryTree,
        Predicate<PostStatus> statusFilter) {
        Assert.notNull(categoryTree, "The categoryTree must not be null.");
        Map<Integer, Set<Integer>> categoryPostIdsMap =
            postCategoryRepository.findAllWithPostStatus()
                .stream()
                .filter(record -> statusFilter.test(record.getPostStatus()))
                .collect(Collectors.groupingBy(CategoryIdPostStatusProjection::getCategoryId,
                    Collectors.mapping(CategoryIdPostStatusProjection::getPostId,
                        Collectors.toSet())));

        walkCategoryTree(categoryTree, category -> {
            // Set post count
            Set<Integer> postIds =
                categoryPostIdsMap.getOrDefault(category.getId(), new LinkedHashSet<>());
            category.setPostIds(postIds);
        });
        CategoryVO categoryTreeRootNode = new CategoryVO();
        categoryTreeRootNode.setChildren(categoryTree);
        mergePostIdsFromBottomToTop(categoryTreeRootNode);
    }

    private void mergePostIdsFromBottomToTop(CategoryVO root) {
        if (root == null) {
            return;
        }
        List<CategoryVO> children = root.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        for (CategoryVO category : children) {
            mergePostIdsFromBottomToTop(category);
            if (root.getPostIds() == null) {
                root.setPostIds(new LinkedHashSet<>());
            }
            // merge post ids.
            root.getPostIds().addAll(category.getPostIds());
        }
    }

    private void walkCategoryTree(List<CategoryVO> categoryTree, Consumer<CategoryVO> consumer) {
        Queue<CategoryVO> queue = new ArrayDeque<>(categoryTree);
        while (!queue.isEmpty()) {
            CategoryVO category = queue.poll();
            consumer.accept(category);
            if (HaloUtils.isNotEmpty(category.getChildren())) {
                queue.addAll(category.getChildren());
            }
        }
    }

    @Override
    public List<PostCategory> listByCategoryIdList(List<Integer> categoryIdList) {
        Assert.notEmpty(categoryIdList, "category id list not empty");
        return postCategoryRepository.findAllByCategoryIdList(categoryIdList);
    }

}
