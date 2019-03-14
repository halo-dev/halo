package cc.ryanc.halo.security.authentication;

import cc.ryanc.halo.security.support.UserDetail;

/**
 * Authentication implementation.
 *
 * @author johnniang
 */
public class AuthenticationImpl implements Authentication {

    private final UserDetail userDetail;

    public AuthenticationImpl(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    @Override
    public UserDetail getDetail() {
        return userDetail;
    }
}
