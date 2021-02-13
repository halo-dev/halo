package run.halo.app.service;

import java.util.Set;

/**
 * @author ZhiXiang Yuan
 * @date 2021/01/20 17:40
 */
public interface AuthorizationService {

    /**
     * Build post token
     *
     * @param postId post id
     * @return token
     */
    static String buildPostToken(Integer postId) {
        return "POST:" + postId;
    }

    /**
     * Build category token
     *
     * @param categoryId category id
     * @return token
     */
    static String buildCategoryToken(Integer categoryId) {
        return "CATEGORY:" + categoryId;
    }

    /**
     * Post authorization
     *
     * @param postId post id
     */
    void postAuthorization(Integer postId);

    /**
     * CategoryAuthorization
     *
     * @param categoryId category id
     */
    void categoryAuthorization(Integer categoryId);

    /**
     * Get access permission store
     *
     * @return access permission store
     */
    Set<String> getAccessPermissionStore();

    /**
     * Delete article authorization
     *
     * @param postId post id
     */
    void deletePostAuthorization(Integer postId);

    /**
     * Delete category Authorization
     *
     * @param categoryId category id
     */
    void deleteCategoryAuthorization(Integer categoryId);
}
