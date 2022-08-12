package run.halo.app.core.extension.service;

import static run.halo.app.core.extension.RoleBinding.containsUser;

import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ReactiveExtensionClient;

@Service
public class UserServiceImpl implements UserService {

    private final ReactiveExtensionClient client;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(ReactiveExtensionClient client, PasswordEncoder passwordEncoder) {
        this.client = client;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> getUser(String username) {
        return client.fetch(User.class, username);
    }

    @Override
    public Mono<User> updatePassword(String username, String newPassword) {
        return getUser(username)
            .filter(user -> !Objects.equals(user.getSpec().getPassword(), newPassword))
            .flatMap(user -> {
                user.getSpec().setPassword(newPassword);
                return client.update(user);
            });
    }

    @Override
    public Mono<User> updateWithRawPassword(String username, String rawPassword) {
        return updatePassword(username, passwordEncoder.encode(rawPassword));
    }

    @Override
    public Flux<Role> listRoles(String name) {
        return client.list(RoleBinding.class, containsUser(name), null)
            .filter(roleBinding -> Role.KIND.equals(roleBinding.getRoleRef().getKind()))
            .map(roleBinding -> roleBinding.getRoleRef().getName())
            .flatMap(roleName -> client.fetch(Role.class, roleName));
    }
}
