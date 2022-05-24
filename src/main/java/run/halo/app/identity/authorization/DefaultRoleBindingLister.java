package run.halo.app.identity.authorization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import run.halo.app.extension.Metadata;

/**
 * <p>Obtain the authorities from the authenticated authentication and construct it as a RoleBinding
 * list.</p>
 * <p>After JWT authentication, the roles stored in the authorities are the roles owned by the user,
 * so there is no need to query from the database.</p>
 * <p>For tokens in other formats, after authentication, fill the authorities with the token into
 * the SecurityContextHolder.</p>
 * <pre>
 * kind: RoleBinding
 * metadata:
 *   name: some-name
 * subjects:
 * # You can specify more than one "subject"
 * - kind: User
 *   name: some-username
 * roleRef:
 *   kind: Role
 *   name: role-name
 * </pre>
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultRoleBindingLister implements RoleBindingLister {
    private static final String SCOPE_AUTHORITY_PREFIX = "SCOPE_";
    private static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

    @Override
    public List<RoleBinding> listRoleBindings() {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        if (authentication == null) {
            return Collections.emptyList();
        }
        String username = authentication.getName();

        List<String> roleNames = roleNamesFromAuthentication();

        List<RoleBinding> roleBindings = new ArrayList<>(roleNames.size());
        for (String roleName : roleNames) {
            RoleBinding roleBinding = new RoleBinding();
            // metadata
            Metadata metadata = new Metadata();
            metadata.setName(username + "_" + roleName);
            roleBinding.setMetadata(metadata);

            // role ref
            RoleRef roleRef = new RoleRef();
            roleRef.setKind("Role");
            roleRef.setName(roleName);
            roleBinding.setRoleRef(roleRef);

            // subject
            Subject subject = new Subject();
            subject.setKind("User");
            subject.setName(username);
            roleBinding.setSubjects(List.of(subject));
            roleBindings.add(roleBinding);
        }
        return roleBindings;
    }

    private List<String> roleNamesFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        return authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map(scope -> {
                if (scope.startsWith(SCOPE_AUTHORITY_PREFIX)) {
                    return scope.replaceFirst(SCOPE_AUTHORITY_PREFIX, "");
                }
                if (scope.startsWith(ROLE_AUTHORITY_PREFIX)) {
                    return scope.replaceFirst(ROLE_AUTHORITY_PREFIX, "");
                }
                return scope;
            })
            .toList();
    }
}
