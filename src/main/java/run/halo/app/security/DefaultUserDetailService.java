package run.halo.app.security;

import java.util.Objects;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
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
            .map(userDetails -> withNewPassword(userDetails, newPassword))
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(userDetails ->
                userService.updatePassword(userDetails.getUsername(), newPassword).subscribe()
            );
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userService.getUser(username).flatMap(user -> {
            final var userGk =
                new run.halo.app.core.extension.User().groupVersionKind().groupKind();
            final var roleGk = new Role().groupVersionKind().groupKind();
            return roleService.listRoleRefs(new Subject(userGk.kind(), username, userGk.group()))
                .filter(roleRef -> Objects.equals(roleGk,
                    new GroupKind(roleRef.getApiGroup(), roleRef.getKind())))
                .map(RoleRef::getName)
                .collectList()
                .map(roleNames -> User.builder()
                    .username(username)
                    .password(user.getSpec().getPassword())
                    .roles(roleNames.toArray(new String[0]))
                    .build());
        });
    }

    private UserDetails withNewPassword(UserDetails userDetails, String newPassword) {
        return User.withUserDetails(userDetails)
            .password(newPassword)
            .build();
    }

}
