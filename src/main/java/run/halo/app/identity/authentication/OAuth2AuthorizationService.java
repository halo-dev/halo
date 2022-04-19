package run.halo.app.identity.authentication;

import org.springframework.lang.Nullable;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface OAuth2AuthorizationService {
    /**
     * Returns the {@link OAuth2Authorization} containing the provided {@code token},
     * or {@code null} if not found.
     *
     * @param token the token credential
     * @param tokenType the {@link OAuth2TokenType token type}
     * @return the {@link OAuth2Authorization} if found, otherwise {@code null}
     */
    @Nullable
    OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType);

    /**
     * Saves the {@link OAuth2Authorization}.
     *
     * @param authorization the {@link OAuth2Authorization}
     */
    void save(OAuth2Authorization authorization);

    /**
     * Removes the {@link OAuth2Authorization}.
     *
     * @param authorization the {@link OAuth2Authorization}
     */
    void remove(OAuth2Authorization authorization);

    /**
     * Returns the {@link OAuth2Authorization} identified by the provided {@code id},
     * or {@code null} if not found.
     *
     * @param id the authorization identifier
     * @return the {@link OAuth2Authorization} if found, otherwise {@code null}
     */
    @Nullable
    OAuth2Authorization findById(String id);

}
