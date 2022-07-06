package run.halo.app.core.extension.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.extension.ExtensionClient;

/**
 * @author guqing
 * @since 2.0.0
 */
@Service
public class DefaultRoleService implements RoleService {

    private final ExtensionClient extensionClient;

    public DefaultRoleService(ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    @Override
    @NonNull
    public Role getRole(@NonNull String name) {
        return extensionClient.fetch(Role.class, name).orElseThrow();
    }

    @Override
    public Flux<RoleRef> listRoleRefs(Subject subject) {
        return Flux.fromIterable(extensionClient.list(RoleBinding.class,
                binding -> binding.getSubjects().contains(subject),
                null))
            .map(RoleBinding::getRoleRef);
    }
}
