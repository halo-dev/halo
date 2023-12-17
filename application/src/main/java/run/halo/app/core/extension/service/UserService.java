package run.halo.app.core.extension.service;

import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;

public interface UserService {

    Mono<User> getUser(String username);

    Mono<User> getUserOrGhost(String username);

    Mono<User> updatePassword(String username, String newPassword);

    Mono<User> updateWithRawPassword(String username, String rawPassword);

    Flux<Role> listRoles(String username);

    Mono<User> grantRoles(String username, Set<String> roles);

    Mono<User> signUp(User user, String password);

    Mono<User> createUser(User user, Set<String> roles);
}
