package run.halo.app.security;

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
        return Mono.just(user)
            .map(userDetails -> withNewPassword(user, newPassword))
            .flatMap(userDetails -> userService.updatePassword(
                    userDetails.getUsername(),
                    userDetails.getPassword())
                .then(Mono.just(userDetails))
            );
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userService.getUser(username).flatMap(user -> {
            final var userGvk =
                new run.halo.app.core.extension.User().groupVersionKind();
            var subject = new Subject(userGvk.kind(), username, userGvk.group());
            return roleService.listRoleRefs(subject)
                .filter(this::isRoleRef)
                .map(RoleRef::getName)
                .collectList()
                .map(roleNames -> User.builder()
                    .username(username)
                    .password(user.getSpec().getPassword())
                    .roles(roleNames.toArray(new String[0]))
                    .build());
        });
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
