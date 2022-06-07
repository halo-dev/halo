package run.halo.app.security.authorization;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class DefaultRoleGetter implements RoleGetter {

    private final ExtensionClient extensionClient;

    public DefaultRoleGetter(ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    @Override
    @NonNull
    public Role getRole(@NonNull String name) {
        return extensionClient.fetch(Role.class, name).orElseThrow();
    }
}
