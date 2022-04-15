package run.halo.app.identity.authentication;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2Token;

/**
 * Implementations of this interface are responsible for generating an {@link OAuth2Token}
 * using the attributes contained in the {@link OAuth2TokenContext}.
 *
 * @param <T> the type of the OAuth 2.0 Token
 * @author guqing
 * @date 2022-04-14
 * @see OAuth2Token
 * @see OAuth2TokenContext
 * @see ClaimAccessor
 */
@FunctionalInterface
public interface OAuth2TokenGenerator<T extends OAuth2Token> {
    /**
     * Generate an OAuth 2.0 Token using the attributes contained in the {@link OAuth2TokenContext},
     * or return {@code null} if the {@link OAuth2TokenContext#getTokenType()} is not supported.
     *
     * <p>
     * If the returned {@link OAuth2Token} has a set of claims, it should implement
     * {@link ClaimAccessor}
     * in order for it to be stored with the {@link OAuth2Authorization}.
     *
     * @param context the context containing the OAuth 2.0 Token attributes
     * @return an {@link OAuth2Token} or {@code null} if the
     * {@link OAuth2TokenContext#getTokenType()} is not supported
     */
    @Nullable
    T generate(OAuth2TokenContext context);
}
