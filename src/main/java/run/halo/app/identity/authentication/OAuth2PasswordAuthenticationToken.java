package run.halo.app.identity.authentication;

import java.util.Map;
import java.util.Set;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * @author guqing
 * @since 2.0.0
 */
public class OAuth2PasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
    private final String username;

    private final String password;

    private final Set<String> scopes;

    public OAuth2PasswordAuthenticationToken(String username, String password,
        Set<String> scopes, Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.PASSWORD, additionalParameters);
        this.username = username;
        this.password = password;
        this.scopes = scopes;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
