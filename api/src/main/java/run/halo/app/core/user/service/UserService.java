package run.halo.app.core.user.service;

import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;

public interface UserService {

    Mono<User> getUser(String username);

    Mono<User> getUserOrGhost(String username);

    Mono<User> updatePassword(String username, String newPassword);

    Mono<User> updateWithRawPassword(String username, String rawPassword);

    Mono<User> grantRoles(String username, Set<String> roles);

    Mono<User> signUp(SignUpData signUpData);

    Mono<User> createUser(User user, Set<String> roles);

    Mono<Boolean> confirmPassword(String username, String rawPassword);

    Flux<User> listByEmail(String email);

    Mono<Boolean> checkEmailAlreadyVerified(String email);

    String encryptPassword(String rawPassword);

    Mono<User> disable(String username);

    Mono<User> enable(String username);

    /**
     * Gets multiple users by usernames in a single batch operation.
     * For non-existent users, returns ghost user instead.
     *
     * @param usernames list of usernames to fetch
     * @return flux of users, with ghost user for non-existent usernames
     */
    Flux<User> getUsersOrGhostByNames(Set<String> usernames);

}
