package run.halo.app.core.user.service;

import reactor.core.publisher.Mono;
import run.halo.app.security.PersonalAccessToken;

/**
 * Service for personal access token.
 *
 * @author johnniang
 */
public interface PatService {

    /**
     * Create a new personal access token. We will automatically use the current user as the
     * owner of the token from the security context.
     *
     * @param patRequest the personal access token request
     * @return the created personal access token
     */
    Mono<PersonalAccessToken> create(PersonalAccessToken patRequest);

    /**
     * Create a new personal access token for the specified user.
     *
     * @param patRequest the personal access token request
     * @param username the username of the user
     * @return the created personal access token
     */
    Mono<PersonalAccessToken> create(PersonalAccessToken patRequest, String username);

    /**
     * Revoke a personal access token.
     *
     * @param patName the name of the personal access token
     * @param username the username of the user
     * @return the revoked personal access token
     */
    Mono<PersonalAccessToken> revoke(String patName, String username);

    /**
     * Restore a personal access token.
     *
     * @param patName the name of the personal access token
     * @param username the username of the user
     * @return the restored personal access token
     */
    Mono<PersonalAccessToken> restore(String patName, String username);

    /**
     * Delete a personal access token.
     *
     * @param patName the name of the personal access token
     * @param username the username of the user
     * @return the deleted personal access token
     */
    Mono<PersonalAccessToken> delete(String patName, String username);

    /**
     * Get a personal access token by name.
     *
     * @param patName the name of the personal access token
     * @param username the username of the user
     * @return the personal access token
     */
    Mono<PersonalAccessToken> get(String patName, String username);

    /**
     * Generate a personal access token.
     *
     * @param pat the personal access token
     * @return the generated token
     */
    Mono<String> generateToken(PersonalAccessToken pat);

}
