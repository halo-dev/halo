package run.halo.app.listener.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.event.category.CategoryUpdatedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.PostService;

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

        List<Post> posts = postService.listAllByIds(event.getPostIds());

        // handle delete action
        if (RecordState.DELETED.equals(recordState) || category == null) {
            if (beforeUpdated == null || !beforeIsPrivate) {
                return;
            }
            // Cancel the encryption status of the posts
            changeStatusToPublishIfNecessary(posts, beforeUpdated.getId());
            return;
        }

        // now
        boolean isPrivate = categoryService.isPrivate(category.getId());
        if (isPrivate) {
            List<Post> postsToUpdate = new ArrayList<>();
            posts.forEach(post -> {
                if (post.getStatus() == PostStatus.PUBLISHED) {
                    post.setStatus(PostStatus.INTIMATE);
                    postsToUpdate.add(post);
                }
            });
            postService.updateInBatch(postsToUpdate);
        } else if (beforeIsPrivate && RecordState.UPDATED.equals(recordState)) {
            // Cancel the encryption status of the posts
            changeStatusToPublishIfNecessary(posts, category.getId());
        }
    }

    private void changeStatusToPublishIfNecessary(List<Post> posts, Integer categoryId) {
        List<Post> postsToUpdate = new ArrayList<>();
        for (Post post : posts) {
            boolean belongsToEncryptedCategory = postBelongsToEncryptedCategory(post.getId());
            if (!belongsToEncryptedCategory && StringUtils.isBlank(post.getPassword())
                && post.getStatus() == PostStatus.INTIMATE) {
                post.setStatus(PostStatus.PUBLISHED);
                postsToUpdate.add(post);
            }
        }
        postService.updateInBatch(postsToUpdate);
    }

    private boolean postBelongsToEncryptedCategory(Integer postId) {
        Set<Integer> categoryIds = postCategoryService.listCategoryIdsByPostId(postId);

        boolean encrypted = false;
        for (Integer categoryId : categoryIds) {
            if (categoryService.isPrivate(categoryId)) {
                encrypted = true;
                break;
            }
        }
        return encrypted;
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

    enum RecordState {
        /**
         * effective state for database record.
         */
        CREATED,
        UPDATED,
        DELETED,
        UNCHANGED
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
        if (PostStatus.RECYCLE.equals(status)) {
            return;
        }
        boolean isPrivate = postCategoryService.listByPostId(post.getId())
            .stream()
            .anyMatch(postCategory -> categoryService.isPrivate(postCategory.getCategoryId()));
        if (post.getStatus() != PostStatus.DRAFT) {
            if (isPrivate || StringUtils.isNotEmpty(post.getPassword())) {
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
