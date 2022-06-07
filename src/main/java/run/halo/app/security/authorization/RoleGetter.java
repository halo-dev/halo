package run.halo.app.security.authorization;

import org.springframework.lang.NonNull;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RoleGetter {

    @NonNull
    Role getRole(String name);
}
