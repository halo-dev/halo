package run.halo.app.identity.authorization;

import java.util.Set;

/**
 * @author guqing
 * @since 2.0.0
 */
@FunctionalInterface
public interface RoleBindingLister {

    Set<String> listBoundRoleNames();
}
