package run.halo.app.controller.content.auth;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.event.category.CategoryUpdatedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.EncryptTypeEnum;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostService;

/**
 * Content authentication manager.
 *
 * @author guqing
 * @date 2022-02-24
 */
@Component
public class ContentAuthenticationManager {
    private final CategoryService categoryService;
    private final CategoryAuthentication categoryAuthentication;
    private final PostService postService;
    private final PostAuthentication postAuthentication;

    public ContentAuthenticationManager(CategoryService categoryService,
        CategoryAuthentication categoryAuthentication, PostService postService,
        PostAuthentication postAuthentication) {
        this.categoryService = categoryService;
        this.categoryAuthentication = categoryAuthentication;
        this.postService = postService;
        this.postAuthentication = postAuthentication;
    }

    public ContentAuthentication authenticate(ContentAuthenticationRequest authRequest) throws
        AuthenticationException {
        if (EncryptTypeEnum.POST.getName().equals(authRequest.getPrincipal())) {
            return authenticatePost(authRequest);
        }
        if (EncryptTypeEnum.CATEGORY.getName().equals(authRequest.getPrincipal())) {
            return authenticateCategory(authRequest);
        }
        throw new NotFoundException(
            "Could not be found suitable authentication processor for ["
                + authRequest.getPrincipal() + "]");
    }

    @EventListener(CategoryUpdatedEvent.class)
    public void categoryUpdatedListener(CategoryUpdatedEvent event) {
        Category category = event.getCategory();
        categoryAuthentication.clearByResourceId(category.getId());
    }

    @EventListener(PostUpdatedEvent.class)
    public void postUpdatedListener(PostUpdatedEvent event) {
        Post post = event.getPost();
        postAuthentication.clearByResourceId(post.getId());
    }

    private PostAuthentication authenticatePost(ContentAuthenticationRequest authRequest) {
        Post post = postService.getById(authRequest.getId());
        if (post.getPassword() == null
            || StringUtils.equals(post.getPassword(), authRequest.getPassword())) {
            postAuthentication.setAuthenticated(post.getId(), true);
            return postAuthentication;
        }
        throw new AuthenticationException("密码不正确");
    }

    private CategoryAuthentication authenticateCategory(ContentAuthenticationRequest authRequest) {
        Category category = categoryService.getById(authRequest.getId());
        if (category.getPassword() == null) {
            String parentPassword = categoryService.lookupFirstEncryptedBy(category.getId())
                .map(Category::getPassword)
                .orElse(null);
            if (parentPassword == null) {
                return categoryAuthentication;
            }
            category.setPassword(parentPassword);
        }

        if (StringUtils.equals(category.getPassword(), authRequest.getPassword())) {
            categoryAuthentication.setAuthenticated(category.getId(), true);
            return categoryAuthentication;
        }
        // Finds the first encrypted parent category to authenticate
        Category parentCategory =
            categoryService.lookupFirstEncryptedBy(authRequest.getId())
                .orElseThrow(() -> new AuthenticationException("密码不正确"));
        if (!Objects.equals(parentCategory.getPassword(), authRequest.getPassword())) {
            throw new AuthenticationException("密码不正确");
        }
        categoryAuthentication.setAuthenticated(category.getId(), true);
        return categoryAuthentication;
    }
}
