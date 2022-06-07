package run.halo.app.security.authorization;

import java.util.Collection;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author guqing
 * @since 2.0.0
 */
@FunctionalInterface
public interface RoleBindingLister {

    Set<String> listBoundRoleNames(Collection<? extends GrantedAuthority> authorities);
}
