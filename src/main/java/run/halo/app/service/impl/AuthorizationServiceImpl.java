package run.halo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.service.AuthorizationService;
import run.halo.app.utils.JsonUtils;

/**
 * @author ZhiXiang Yuan
 * @author guqing
 * @date 2021/01/21 11:28
 */
@Slf4j
@Service
public class AuthorizationServiceImpl implements AuthorizationService {
    private static final String ACCESS_PERMISSION_PREFIX = "ACCESS_PERMISSION: ";
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

        for (Entry<String, String> entry : cacheStore.toMap().entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(ACCESS_PERMISSION_PREFIX)) {
                continue;
            }
            Set<String> valueSet = jsonToValueSet(entry.getValue());
            if (valueSet.contains(value)) {
                valueSet.remove(value);
                cacheStore.putAny(key, valueSet, 1, TimeUnit.DAYS);
            }
        }
    }

    private Set<String> jsonToValueSet(String json) {
        try {
            return JsonUtils.DEFAULT_JSON_MAPPER.readValue(json,
                new TypeReference<LinkedHashSet<String>>() {
                });
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert json to authorization cache value set: [{}]", json, e);
        }
        return Collections.emptySet();
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

        return ACCESS_PERMISSION_PREFIX + request.getSession().getId();
    }

}
