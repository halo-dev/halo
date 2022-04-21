package run.halo.app.identity.authentication.verifier;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

/**
 * An {@link Authentication} that contains a
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>.
 *
 * @author guqing
 * @since 2.0.0
 */
public class BearerTokenAuthenticationToken extends AbstractAuthenticationToken {
    private final String token;

    /**
     * Create a {@code BearerTokenAuthenticationToken} using the provided parameter(s)
     *
     * @param token - the bearer token
     */
    public BearerTokenAuthenticationToken(String token) {
        super(Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        this.token = token;
    }

    /**
     * Get the
     * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>
     *
     * @return the token that proves the caller's authority to perform the
     * {@link jakarta.servlet.http.HttpServletRequest}
     */
    public String getToken() {
        return this.token;
    }

    @Override
    public Object getCredentials() {
        return this.getToken();
    }

    @Override
    public Object getPrincipal() {
        return this.getToken();
    }
}
