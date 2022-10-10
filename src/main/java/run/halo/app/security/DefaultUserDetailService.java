package run.halo.app.security;

import static run.halo.app.core.extension.User.GROUP;
import static run.halo.app.core.extension.User.KIND;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.GroupKind;
import run.halo.app.extension.exception.ExtensionNotFoundException;

public class DefaultUserDetailService
    implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private final UserService userService;

    private final RoleService roleService;

    public DefaultUserDetailService(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
        return userService.updatePassword(user.getUsername(), newPassword)
            .map(u -> withNewPassword(user, newPassword));
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userService.getUser(username)
            .flatMap(user -> {
                var subject = new Subject(KIND, username, GROUP);
                return roleService.listRoleRefs(subject)
                    .filter(this::isRoleRef)
                    .map(RoleRef::getName)
                    .collectList()
                    .map(roleNames -> User.builder()
                        .username(username)
                        .password(user.getSpec().getPassword())
                        .roles(roleNames.toArray(new String[0]))
                        .build());
            })
            .onErrorMap(ExtensionNotFoundException.class,
                e -> new BadCredentialsException("Invalid Credentials"));
    }

    private boolean isRoleRef(RoleRef roleRef) {
        var roleGvk = new Role().groupVersionKind();
        var gk = new GroupKind(roleRef.getApiGroup(), roleRef.getKind());
        return gk.equals(roleGvk.groupKind());
    }

    private UserDetails withNewPassword(UserDetails userDetails, String newPassword) {
        return User.withUserDetails(userDetails)
            .password(newPassword)
            .build();
    }

}
