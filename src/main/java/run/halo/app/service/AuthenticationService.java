package run.halo.app.service;

import run.halo.app.model.entity.Post;

/**
 * Authentication service
 *
 * @author ZhiXiang Yuan
 * @date 2021/01/20 17:39
 */
public interface AuthenticationService {

    /**
     * post authentication
     *
     * @param post     post
     * @param password password
     * @return authentication success or fail
     */
    boolean postAuthentication(Post post, String password);

    /**
     * category authentication
     *
     * @param categoryId category id
     * @param password   password
     * @return authentication success or fail
     */
    boolean categoryAuthentication(Integer categoryId, String password);

}
