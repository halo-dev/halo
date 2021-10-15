package run.halo.app.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.service.AuthorizationService;

/**
 * @author ZhiXiang Yuan
 * @date 2021/01/21 11:28
 */
@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AbstractStringCacheStore cacheStore;

    public AuthorizationServiceImpl(AbstractStringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public void postAuthorization(Integer postId) {
        doAuthorization(AuthorizationService.buildPostToken(postId));
    }

    @Override
    public void categoryAuthorization(Integer categoryId) {
        doAuthorization(AuthorizationService.buildCategoryToken(categoryId));
    }

    @Override
    public Set<String> getAccessPermissionStore() {
        return cacheStore.getAny(buildAccessPermissionKey(), Set.class).orElseGet(HashSet::new);
    }

    @Override
    public void deletePostAuthorization(Integer postId) {
        doDeleteAuthorization(AuthorizationService.buildPostToken(postId));
    }

    @Override
    public void deleteCategoryAuthorization(Integer categoryId) {
        doDeleteAuthorization(AuthorizationService.buildCategoryToken(categoryId));
    }

    private void doDeleteAuthorization(String value) {
        Set<String> accessStore = getAccessPermissionStore();

        accessStore.remove(value);

        cacheStore.putAny(buildAccessPermissionKey(), accessStore, 1, TimeUnit.DAYS);
    }

    private void doAuthorization(String value) {
        Set<String> accessStore = getAccessPermissionStore();

        accessStore.add(value);

        cacheStore.putAny(buildAccessPermissionKey(), accessStore, 1, TimeUnit.DAYS);
    }

    private String buildAccessPermissionKey() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();

        HttpServletRequest request = requestAttributes.getRequest();

        return "ACCESS_PERMISSION: " + request.getSession().getId();
    }

}
