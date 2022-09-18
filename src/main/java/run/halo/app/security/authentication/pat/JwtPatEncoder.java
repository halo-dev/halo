package run.halo.app.security.authentication.pat;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.pat.PersonalAccessToken;

@Component
public class JwtPatEncoder implements PatEncoder {

    private final JwtEncoder jwtEncoder;

    private final PasswordEncoder passwordEncoder;

    public JwtPatEncoder(JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<String> encode(PersonalAccessToken pat) {
        return Mono.error(UnsupportedOperationException::new);
    }

    @Override
    public boolean matches(String rawToken, String encodedToken) {
        return passwordEncoder.matches(rawToken, encodedToken);
    }
}
