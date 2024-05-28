package run.halo.app.security;

import static java.util.Objects.requireNonNullElse;
import static run.halo.app.core.extension.User.GROUP;
import static run.halo.app.core.extension.User.KIND;
import static run.halo.app.security.authorization.AuthorityUtils.ANONYMOUS_ROLE_NAME;
import static run.halo.app.security.authorization.AuthorityUtils.AUTHENTICATED_ROLE_NAME;
import static run.halo.app.security.authorization.AuthorityUtils.ROLE_PREFIX;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import run.halo.app.infra.exception.UserNotFoundException;
import run.halo.app.security.authentication.login.HaloUser;

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
            .onErrorMap(UserNotFoundException.class,
                e -> new BadCredentialsException("Invalid Credentials"))
            .flatMap(user -> {
                var name = user.getMetadata().getName();
                var subject = new Subject(KIND, name, GROUP);
                var userBuilder = User.withUsername(name)
                    .password(user.getSpec().getPassword())
                    .disabled(requireNonNullElse(user.getSpec().getDisabled(), false));
                var setAuthorities = roleService.listRoleRefs(subject)
                    .filter(this::isRoleRef)
                    .map(RoleRef::getName)
                    // every authenticated user should have authenticated and anonymous roles.
                    .concatWithValues(AUTHENTICATED_ROLE_NAME, ANONYMOUS_ROLE_NAME)
                    .map(roleName -> new SimpleGrantedAuthority(ROLE_PREFIX + roleName))
                    .distinct()
                    .collectList()
                    .doOnNext(userBuilder::authorities);

                return setAuthorities.then(Mono.fromSupplier(() -> {
                    var twoFactorAuthEnabled =
                        requireNonNullElse(user.getSpec().getTwoFactorAuthEnabled(), false);
                    return new HaloUser.Builder(userBuilder.build())
                        .twoFactorAuthEnabled(twoFactorAuthEnabled)
                        .totpEncryptedSecret(user.getSpec().getTotpEncryptedSecret())
                        .build();
                }));
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
