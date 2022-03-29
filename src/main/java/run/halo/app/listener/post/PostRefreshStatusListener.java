package run.halo.app.listener.post;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.event.category.CategoryUpdatedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.PostService;
import run.halo.app.utils.ServiceUtils;

/**
 * Post status management.
 *
 * @author guqing
 * @date 2022-02-28
 */
@Component
public class PostRefreshStatusListener {

    private final PostService postService;
    private final CategoryService categoryService;
    private final PostCategoryService postCategoryService;

    public PostRefreshStatusListener(PostService postService,
        CategoryService categoryService,
        PostCategoryService postCategoryService) {
        this.postService = postService;
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
    }

    /**
     * If the current category is encrypted, refresh all post referencing the category to
     * INTIMATE status.
     *
     * @param event category updated event
     */
    @EventListener(CategoryUpdatedEvent.class)
    public void categoryUpdatedListener(CategoryUpdatedEvent event) {
        Category category = event.getCategory();
        Category beforeUpdated = event.getBeforeUpdated();
        boolean beforeIsPrivate = event.isBeforeIsPrivate();
        RecordState recordState = determineRecordState(beforeUpdated, category);
        if (RecordState.DELETED.equals(recordState) || category == null) {
            return;
        }

        // now
        boolean isPrivate = categoryService.isPrivate(category.getId());
        List<Post> posts = findPostsByCategoryIdRecursively(category.getId());
        if (isPrivate) {
            posts.forEach(post -> {
                if (post.getStatus() == PostStatus.PUBLISHED) {
                    post.setStatus(PostStatus.INTIMATE);
                }
            });
        } else {
            if (RecordState.UPDATED.equals(recordState)) {
                Set<Integer> encryptedCategories =
                    pickUpEncryptedFromUpdatedRecord(category.getId());
                for (Post post : posts) {
                    boolean belongsToEncryptedCategory =
                        postBelongsToEncryptedCategory(post.getId(), encryptedCategories);
                    if (!belongsToEncryptedCategory && StringUtils.isBlank(post.getPassword())
                        && beforeIsPrivate
                        && post.getStatus() == PostStatus.INTIMATE) {
                        post.setStatus(PostStatus.PUBLISHED);
                    }
                }
            }
        }
        postService.updateInBatch(posts);
    }

    private boolean postBelongsToEncryptedCategory(Integer postId,
        Set<Integer> encryptedCategories) {
        Set<Integer> categoryIds =
            postCategoryService.listCategoryIdsByPostId(postId);

        boolean encrypted = false;
        for (Integer categoryId : categoryIds) {
            if (encryptedCategories.contains(categoryId)) {
                encrypted = true;
                break;
            }
        }
        return encrypted;
    }

    private Set<Integer> pickUpEncryptedFromUpdatedRecord(Integer categoryId) {
        Set<Integer> privateCategories = new HashSet<>();

        List<Category> categories = categoryService.listAllByParentId(categoryId);
        Map<Integer, Category> categoryMap =
            ServiceUtils.convertToMap(categories, Category::getId);
        categories.forEach(category -> {
            boolean privateBy = isPrivateBy(category.getId(), categoryMap);
            if (privateBy) {
                privateCategories.add(category.getId());
            }
        });
        return privateCategories;
    }

    private boolean isPrivateBy(Integer categoryId, Map<Integer, Category> categoryMap) {
        return findFirstEncryptedCategoryBy(categoryMap, categoryId) != null;
    }

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

    private RecordState determineRecordState(Category before, Category updated) {
        if (before == null) {
            if (updated != null) {
                // created: null -> record
                return RecordState.CREATED;
            } else {
                // unchanged: null -> null
                return RecordState.UNCHANGED;
            }
        } else {
            if (updated == null) {
                // deleted: record -> null
                return RecordState.DELETED;
            } else {
                // updated: record -> record
                return RecordState.UPDATED;
            }
        }
    }

    /**
     * effective state for database record.
     */
    enum RecordState {
        CREATED,
        UPDATED,
        DELETED,
        UNCHANGED
    }

    @NonNull
    private List<Post> findPostsByCategoryIdRecursively(Integer categoryId) {
        Set<Integer> categoryIds =
            ServiceUtils.fetchProperty(categoryService.listAllByParentId(categoryId),
                Category::getId);
        List<PostCategory> postCategories =
            postCategoryService.listByCategoryIdList(new ArrayList<>(categoryIds));
        Set<Integer> postIds = ServiceUtils.fetchProperty(postCategories, PostCategory::getPostId);
        return postService.listAllByIds(postIds);
    }

    /**
     * If the post belongs to any encryption category, set the status to INTIMATE.
     *
     * @param event post updated event
     */
    @EventListener(PostUpdatedEvent.class)
    public void postUpdatedListener(PostUpdatedEvent event) {
        Post post = event.getPost();
        if (!postService.existsById(post.getId())) {
            return;
        }

        PostStatus status = post.getStatus();
        boolean isPrivate = postCategoryService.listByPostId(post.getId())
            .stream()
            .anyMatch(postCategory -> categoryService.isPrivate(postCategory.getCategoryId()));
        if (post.getStatus() != PostStatus.DRAFT) {
            if (StringUtils.isNotEmpty(post.getPassword())) {
                status = PostStatus.INTIMATE;
            } else if (isPrivate) {
                status = PostStatus.INTIMATE;
            } else {
                status = PostStatus.PUBLISHED;
            }
        } else if (!isPrivate && StringUtils.isBlank(post.getPassword())) {
            status = PostStatus.DRAFT;
        }
        post.setStatus(status);
        postService.update(post);
    }
}
