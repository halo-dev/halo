package run.halo.app.identity.authorization;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * <p>Obtain the authorities from the authenticated authentication and construct it as a RoleBinding
 * list.</p>
 * <p>After JWT authentication, the roles stored in the authorities are the roles owned by the user,
 * so there is no need to query from the database.</p>
 * <p>For tokens in other formats, after authentication, fill the authorities with the token into
 * the SecurityContextHolder.</p>
 *
 * @author guqing
 * @see AnonymousAuthenticationFilter
 * @since 2.0.0
 */
@Slf4j
public class DefaultRoleBindingLister implements RoleBindingLister {
    private static final String SCOPE_AUTHORITY_PREFIX = "SCOPE_";
    private static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

    @Override
    public Set<String> listBoundRoleNames() {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        if (authentication == null) {
            log.debug("No authentication found in SecurityContext.");
            return Collections.emptySet();
        }
        return authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            // Exclude anonymous user roles
            .filter(authority -> !authority.equals("ROLE_ANONYMOUS"))
            .map(scope -> {
                if (scope.startsWith(SCOPE_AUTHORITY_PREFIX)) {
                    return scope.replaceFirst(SCOPE_AUTHORITY_PREFIX, "");
                }
                if (scope.startsWith(ROLE_AUTHORITY_PREFIX)) {
                    return scope.replaceFirst(ROLE_AUTHORITY_PREFIX, "");
                }
                return scope;
            })
            .collect(Collectors.toSet());
    }
}
