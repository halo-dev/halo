package run.halo.app.controller.content.auth;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.category.CategoryUpdatedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.EncryptTypeEnum;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;
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
    private final PostCategoryService postCategoryService;

    public ContentAuthenticationManager(CategoryService categoryService,
        CategoryAuthentication categoryAuthentication, PostService postService,
        PostAuthentication postAuthentication,
        PostCategoryService postCategoryService) {
        this.categoryService = categoryService;
        this.categoryAuthentication = categoryAuthentication;
        this.postService = postService;
        this.postAuthentication = postAuthentication;
        this.postCategoryService = postCategoryService;
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
        // updated category is null when remove event emited
        if (category == null) {
            category = event.getBeforeUpdated();
        }
        if (category != null) {
            categoryAuthentication.clearByResourceId(category.getId());
        }
    }

    @EventListener(PostUpdatedEvent.class)
    public void postUpdatedListener(PostUpdatedEvent event) {
        Post post = event.getPost();
        if (post != null) {
            postAuthentication.clearByResourceId(post.getId());
        }
    }

    private PostAuthentication authenticatePost(ContentAuthenticationRequest authRequest) {
        Post post = postService.getById(authRequest.getId());
        if (StringUtils.isNotBlank(post.getPassword())) {
            if (StringUtils.equals(post.getPassword(), authRequest.getPassword())) {
                postAuthentication.setAuthenticated(post.getId(), true);
                return postAuthentication;
            } else {
                throw new AuthenticationException("密码不正确");
            }
        } else {
            List<Category> encryptedCategories = postCategoryService.listCategoriesBy(post.getId())
                .stream()
                .filter(category -> categoryService.isPrivate(category.getId()))
                .collect(Collectors.toList());
            // The post has no password and does not belong to any encryption categories.
            // Return it directly
            if (CollectionUtils.isEmpty(encryptedCategories)) {
                return postAuthentication;
            }

            // Try all categories until the password is correct
            for (Category category : encryptedCategories) {
                if (StringUtils.equals(category.getPassword(), authRequest.getPassword())) {
                    postAuthentication.setAuthenticated(post.getId(), true);
                    return postAuthentication;
                }
            }

            for (Category category : encryptedCategories) {
                boolean authenticated = categoryService.lookupFirstEncryptedBy(category.getId())
                    .filter(parentCategory -> StringUtils.equals(parentCategory.getPassword(),
                        authRequest.getPassword()))
                    .isPresent();

                if (authenticated) {
                    postAuthentication.setAuthenticated(post.getId(), true);
                    return postAuthentication;
                }
            }
            throw new AuthenticationException("密码不正确");
        }
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
