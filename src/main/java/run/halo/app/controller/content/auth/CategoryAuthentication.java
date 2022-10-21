package run.halo.app.controller.content.auth;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.model.entity.Category;
import run.halo.app.model.enums.EncryptTypeEnum;
import run.halo.app.service.CategoryService;

/**
 * Authentication for category.
 *
 * @author guqing
 * @date 2022-02-23
 */
@Component
public class CategoryAuthentication implements ContentAuthentication {

    private final CategoryService categoryService;

    private final AbstractStringCacheStore cacheStore;

    public CategoryAuthentication(CategoryService categoryService,
        AbstractStringCacheStore cacheStore) {
        this.categoryService = categoryService;
        this.cacheStore = cacheStore;
    }

    @Override
    @NonNull
    public Object getPrincipal() {
        return EncryptTypeEnum.CATEGORY.getName();
    }

    @Override
    public boolean isAuthenticated(Integer categoryId) {
        Category category = categoryService.getById(categoryId);
        if (!isPrivate(category)) {
            return true;
        }

        String sessionId = getSessionId();
        // No session is represent a client request
        if (StringUtils.isEmpty(sessionId)) {
            return false;
        }

        String cacheKey =
            buildCacheKey(sessionId, getPrincipal().toString(), String.valueOf(categoryId));
        return cacheStore.get(cacheKey).isPresent();
    }

    private boolean isPrivate(Category category) {
        if (StringUtils.isNotBlank(category.getPassword())) {
            return true;
        }
        return categoryService.lookupFirstEncryptedBy(category.getId()).isPresent();
    }

    @Override
    public void setAuthenticated(Integer resourceId, boolean isAuthenticated) {
        String sessionId = getSessionId();
        // No session is represent a client request
        if (StringUtils.isEmpty(sessionId)) {
            return;
        }

        String cacheKey =
            buildCacheKey(sessionId, getPrincipal().toString(), String.valueOf(resourceId));
        if (isAuthenticated) {
            cacheStore.putAny(cacheKey, StringUtils.EMPTY, 1, TimeUnit.DAYS);
            return;
        }
        cacheStore.delete(cacheKey);
    }

    @Override
    public void clearByResourceId(Integer resourceId) {
        String sessionId = getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            return;
        }
        String categoryCacheKey =
            buildCacheKey(sessionId, getPrincipal().toString(), String.valueOf(resourceId));
        // clean category cache
        cacheStore.delete(categoryCacheKey);

        Set<Integer> postIds = categoryService.listPostIdsByCategoryIdRecursively(resourceId);
        Set<String> postCacheKeys = postIds.stream()
            .map(postId ->
                buildCacheKey(sessionId, EncryptTypeEnum.POST.getName(), String.valueOf(postId)))
            .collect(Collectors.toSet());
        // clean category post cache
        postCacheKeys.forEach(cacheStore::delete);
    }
}
