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

    String encryptPassword(String rawPassword);
}
