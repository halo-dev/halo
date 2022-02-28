package run.halo.app.listener.post;

import java.util.List;
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
        if (!categoryService.existsById(category.getId())) {
            return;
        }
        boolean isPrivate = categoryService.isPrivate(category.getId());
        if (!isPrivate) {
            return;
        }
        List<Post> posts = postCategoryService.listPostBy(category.getId());
        posts.forEach(post -> {
            post.setStatus(PostStatus.INTIMATE);
        });
        postService.updateInBatch(posts);
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
        boolean isPrivate = postCategoryService.listByPostId(post.getId())
            .stream()
            .anyMatch(postCategory -> categoryService.isPrivate(postCategory.getCategoryId()));

        if (isPrivate) {
            post.setStatus(PostStatus.INTIMATE);
            postService.update(post);
        }
    }
}
