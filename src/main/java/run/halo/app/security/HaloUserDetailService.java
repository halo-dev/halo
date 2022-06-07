package run.halo.app.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ExtensionClient;

public class HaloUserDetailService
    implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private final UserService userService;

    private final ExtensionClient extensionClient;

    public HaloUserDetailService(UserService userService, ExtensionClient extensionClient) {
        this.userService = userService;
        this.extensionClient = extensionClient;
    }

    @Override
    public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
        return Mono.just(user)
            .map(userDetails -> withNewPassword(userDetails, newPassword))
            .doOnNext(
                userDetails -> {
                    var userToUpdate = extensionClient
                        .fetch(run.halo.app.core.extension.User.class, user.getUsername())
                        .orElseThrow(
                            () -> new BadCredentialsException("Invalid username or password"));
                    userToUpdate.getSpec().setPassword(newPassword);
                    extensionClient.update(userToUpdate);
                });
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        var user = extensionClient.fetch(run.halo.app.core.extension.User.class, username)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // TODO list all roles
        extensionClient.list(RoleBinding.class, null, null);

        var userDetails = User.builder()
            .username(username)
            .password(user.getSpec().getPassword())
            .build();
        return Mono.just(userDetails);
    }

    private UserDetails withNewPassword(UserDetails userDetails, String newPassword) {
        return User.withUserDetails(userDetails)
            .password(newPassword)
            .build();
    }

}
