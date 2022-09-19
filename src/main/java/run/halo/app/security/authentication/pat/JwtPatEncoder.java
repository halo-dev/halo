package run.halo.app.security.authentication.pat;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.pat.PersonalAccessToken;

@Component
public class JwtPatEncoder implements PatEncoder {

    public static final String PAT_PREFIX = "pat_";

    public static final String CLAIM_NAME = "patName";

    private final JwtEncoder jwtEncoder;

    private final PasswordEncoder passwordEncoder;

    public JwtPatEncoder(JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<String> buildToken(PersonalAccessToken pat) {
        return Mono.fromCallable(() -> {
            var claims = JwtClaimsSet.builder()
                .claim(CLAIM_NAME, pat.getMetadata().getName())
                .build();
            var jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));
            return PAT_PREFIX + jwt.getTokenValue();
        });
    }

    @Override
    public Mono<String> encode(String rawToken) {
        return Mono.fromCallable(() -> passwordEncoder.encode(rawToken));
    }

    @Override
    public Mono<Boolean> matches(String rawToken, String encodedToken) {
        return Mono.fromCallable(() -> passwordEncoder.matches(rawToken, encodedToken));
    }
}
