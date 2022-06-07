package run.halo.app.core.extension.service;

import org.springframework.lang.NonNull;
import run.halo.app.core.extension.Role;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RoleGetter {

    @NonNull
    Role getRole(String name);
}
