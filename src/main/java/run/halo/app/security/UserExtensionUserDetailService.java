package run.halo.app.security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.Constant;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;

public class UserExtensionUserDetailService
    implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private final UserService userService;

    private final RoleService roleService;

    public UserExtensionUserDetailService(UserService userService, RoleService roleService) {
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
        return userService.getUser(username).flatMap(user ->
            roleService.listRoleRefs(new Subject("User", username, Constant.RBAC_GROUP))
                .filter(roleRef -> Constant.RBAC_GROUP.equals(roleRef.getApiGroup()))
                .filter(roleRef -> "Role".equals(roleRef.getKind()))
                .map(RoleRef::getName)
                .collectList()
                .map(roleNames -> User.builder()
                    .username(username)
                    .password(user.getSpec().getPassword())
                    .roles(roleNames.toArray(new String[0]))
                    .build()));
    }

    private UserDetails withNewPassword(UserDetails userDetails, String newPassword) {
        return User.withUserDetails(userDetails)
            .password(newPassword)
            .build();
    }

}
