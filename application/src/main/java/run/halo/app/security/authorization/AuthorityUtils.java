package run.halo.app.security.authorization;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Utility methods for manipulating GrantedAuthority collection.
 *
 * @author johnniang
 */
public enum AuthorityUtils {
    ;

    public static final String SCOPE_PREFIX = "SCOPE_";

    public static final String ROLE_PREFIX = "ROLE_";

    public static final String SUPER_ROLE_NAME = "super-role";

    public static final String AUTHENTICATED_ROLE_NAME = "authenticated";

    public static final String ANONYMOUS_ROLE_NAME = "anonymous";

    public static final String COMMENT_MANAGEMENT_ROLE_NAME = "role-template-manage-comments";

    /**
     * Converts an array of GrantedAuthority objects to a role set.
     *
     * @return a Set of the Strings obtained from each call to
     * GrantedAuthority.getAuthority() and filtered by prefix "ROLE_".
     */
    public static Set<String> authoritiesToRoles(
        Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> StringUtils.startsWith(authority, ROLE_PREFIX))
            .map(authority -> StringUtils.removeStart(authority, ROLE_PREFIX))
            .collect(Collectors.toSet());
    }

    public static boolean containsSuperRole(Collection<String> roles) {
        return roles.contains(SUPER_ROLE_NAME);
    }

    /**
     * Check if the authentication is a real user.
     *
     * @param authentication current authentication
     * @return true if the authentication is a real user; false otherwise
     */
    public static boolean isRealUser(Authentication authentication) {
        return authentication instanceof UsernamePasswordAuthenticationToken
            || authentication instanceof RememberMeAuthenticationToken;
    }
}
