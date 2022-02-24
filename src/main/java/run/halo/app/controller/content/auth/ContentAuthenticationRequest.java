package run.halo.app.controller.content.auth;

import lombok.Data;

/**
 * Authentication request for {@link ContentAuthenticationManager}.
 *
 * @author guqing
 * @date 2022-02-24
 */
@Data
public class ContentAuthenticationRequest implements ContentAuthentication {
    private Integer id;

    private String password;

    private String principal;

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isAuthenticated(Integer resourceId) {
        return false;
    }

    @Override
    public void setAuthenticated(Integer resourceId, boolean isAuthenticated) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearByResourceId(Integer resourceId) {
        throw new UnsupportedOperationException();
    }
}
