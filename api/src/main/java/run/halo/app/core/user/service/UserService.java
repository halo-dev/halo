package run.halo.app.core.user.service;

import java.util.Collection;
import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;

public interface UserService {

    String GHOST_USER_NAME = "ghost";

    Mono<User> getUser(String username);

    Mono<User> getUserOrGhost(String username);

    Flux<User> getUsersOrGhosts(Collection<String> names);

    Mono<User> updatePassword(String username, String newPassword);

    Mono<User> updateWithRawPassword(String username, String rawPassword);

    Mono<User> grantRoles(String username, Set<String> roles);

    /**
     * Check if the user has sufficient roles.
     *
     * @param roles roles to check
     * @return a Mono that emits true if the user has all the roles, false otherwise
     */
    Mono<Boolean> hasSufficientRoles(Collection<String> roles);

    Mono<User> signUp(SignUpData signUpData);

    Mono<User> createUser(User user, Set<String> roles);

    Mono<Boolean> confirmPassword(String username, String rawPassword);

    Flux<User> listByEmail(String email);

    Mono<Boolean> checkEmailAlreadyVerified(String email);

    String encryptPassword(String rawPassword);

    Mono<User> disable(String username);

    Mono<User> enable(String username);

}
