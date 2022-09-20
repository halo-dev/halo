package run.halo.app.security.authentication.pat;

import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.pat.PersonalAccessToken;
import run.halo.app.infra.properties.JwtProperties;

@Component
public class JwtPatEncoder implements PatEncoder {

    public static final String PAT_PREFIX = "pat_";

    public static final String CLAIM_NAME = "patName";

    private final JwtEncoder jwtEncoder;

    private final PasswordEncoder passwordEncoder;

    private final JwtProperties jwtProps;

    public JwtPatEncoder(JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder,
                         JwtProperties jwtProps) {
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
        this.jwtProps = jwtProps;
    }

    @Override
    public Mono<String> buildToken(PersonalAccessToken pat) {
        return Mono.fromCallable(() -> {
            var header = JwsHeader.with(jwtProps.getJwsAlgorithm()).build();
            var claimsBuilder = JwtClaimsSet.builder();
            if (pat.getSpec().getExpiresAt() != null) {
                claimsBuilder.expiresAt(pat.getSpec().getExpiresAt());
            }
            var claims = claimsBuilder
                .claim(CLAIM_NAME, pat.getMetadata().getName())
                .subject(pat.getSpec().getCreatedBy())
                .issuedAt(Instant.now())
                .build();
            var jwt = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
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
