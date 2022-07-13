package run.halo.app.core.extension.service;

import static run.halo.app.core.extension.RoleBinding.containsUser;

import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;

@Service
public class UserServiceImpl implements UserService {

    private final ExtensionClient client;

    public UserServiceImpl(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<User> getUser(String username) {
        return Mono.justOrEmpty(client.fetch(User.class, username));
    }

    @Override
    public Mono<Void> updatePassword(String username, String newPassword) {
        return getUser(username)
            .doOnNext(user -> {
                user.getSpec().setPassword(newPassword);
                client.update(user);
            })
            .then();
    }

    @Override
    public Flux<Role> listRoles(String name) {
        return Flux.fromStream(client.list(RoleBinding.class, containsUser(name), null)
            .stream()
            .filter(roleBinding -> Role.KIND.equals(roleBinding.getRoleRef().getKind()))
            .map(roleBinding -> roleBinding.getRoleRef().getName())
            .map(roleName -> client.fetch(Role.class, roleName).orElse(null))
            .filter(Objects::nonNull));
    }
}
