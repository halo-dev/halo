package run.halo.app.service.impl;

import cn.hutool.core.util.StrUtil;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.repository.CategoryRepository;
import run.halo.app.repository.PostCategoryRepository;
import run.halo.app.service.AuthenticationService;
import run.halo.app.service.AuthorizationService;


/**
 * @author ZhiXiang Yuan
 * @date 2021/01/20 17:56
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final CategoryRepository categoryRepository;

    private final AuthorizationService authorizationService;

    private final PostCategoryRepository postCategoryRepository;

    public AuthenticationServiceImpl(PostCategoryRepository postCategoryRepository,
        CategoryRepository categoryRepository,
        AuthorizationService authorizationService
    ) {
        this.postCategoryRepository = postCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public boolean postAuthentication(Post post, String password) {
        Set<String> accessPermissionStore = authorizationService.getAccessPermissionStore();

        if (StrUtil.isNotBlank(post.getPassword())) {
            if (accessPermissionStore.contains(AuthorizationService.buildPostToken(post.getId()))) {
                return true;
            }

            if (post.getPassword().equals(password)) {
                authorizationService.postAuthorization(post.getId());
                return true;
            }
            return false;
        }

        Set<Integer> allCategoryIdSet = postCategoryRepository
            .findAllCategoryIdsByPostId(post.getId());

        if (allCategoryIdSet.isEmpty()) {
            return true;
        }

        for (Integer categoryId : allCategoryIdSet) {
            if (categoryAuthentication(categoryId, password)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean categoryAuthentication(Integer categoryId, String password) {

        Map<Integer, Category> idToCategoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        Set<String> accessPermissionStore = authorizationService.getAccessPermissionStore();

        return doCategoryAuthentication(
            idToCategoryMap, accessPermissionStore, categoryId, password);
    }

    private boolean doCategoryAuthentication(Map<Integer, Category> idToCategoryMap,
            Set<String> accessPermissionStore,
            Integer categoryId, String password) {

        Category category = idToCategoryMap.get(categoryId);

        if (StrUtil.isNotBlank(category.getPassword())) {
            if (accessPermissionStore.contains(
                AuthorizationService.buildCategoryToken(category.getId()))) {
                return true;
            }

            if (category.getPassword().equals(password)) {
                authorizationService.categoryAuthorization(category.getId());
                return true;
            }

            return false;
        }

        if (category.getParentId() == 0) {
            return true;
        }

        return doCategoryAuthentication(
            idToCategoryMap, accessPermissionStore, category.getParentId(), password);
    }
}
