package run.halo.app.security.authentication.jwt;

import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class LoginAuthenticationManager
    extends UserDetailsRepositoryReactiveAuthenticationManager {

    public LoginAuthenticationManager(ReactiveUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        super(userDetailsService);
        super.setPasswordEncoder(passwordEncoder);
        if (userDetailsService instanceof ReactiveUserDetailsPasswordService passwordService) {
            super.setUserDetailsPasswordService(passwordService);
        }
    }
}
