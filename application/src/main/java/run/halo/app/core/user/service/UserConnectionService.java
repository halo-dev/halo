package run.halo.app.core.user.service;

import org.springframework.security.oauth2.core.user.OAuth2User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.UserConnection;

public interface UserConnectionService {

    /**
     * Create user connection.
     *
     * @param username Username
     * @param registrationId Registration id
     * @param oauth2User OAuth2 user
     * @return Created user connection
     */
    Mono<UserConnection> createUserConnection(
            String username, String registrationId, OAuth2User oauth2User);

    /**
     * Update the user connection if present.
     * If found, update updatedAt timestamp of the user connection.
     *
     * @param registrationId Registration id
     * @param oauth2User OAuth2 user
     * @return Updated user connection or empty
     */
    Mono<UserConnection> updateUserConnectionIfPresent(
            String registrationId, OAuth2User oauth2User);

    /**
     * Remove user connection.
     *
     * @param registrationId Registration ID
     * @param username Username
     * @return A list of user connections
     */
    Flux<UserConnection> removeUserConnection(String registrationId, String username);

    /**
     * Find user connection by provider user id.
     *
     * @param registrationId registration id
     * @param providerUserId provider user id such as openid
     * @return matched user connection or empty
     */
    Mono<UserConnection> getByProviderUserId(String registrationId, String providerUserId);

    /**
     * Remove user connection by provider user id.
     *
     * @param registrationId the registration id
     * @param providerUserId the provider user id
     * @return empty mono
     */
    Mono<Void> removeByProviderUserId(String registrationId, String providerUserId);
}
