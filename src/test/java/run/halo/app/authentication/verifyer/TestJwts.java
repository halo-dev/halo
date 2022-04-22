package run.halo.app.authentication.verifyer;

import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @author guqing
 * @since 2.0.0
 */
public final class TestJwts {

    private TestJwts() {
    }

    public static Jwt.Builder jwt() {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .audience(List.of("https://audience.example.org"))
            .expiresAt(Instant.MAX)
            .issuedAt(Instant.MIN)
            .issuer("https://issuer.example.org")
            .jti("jti")
            .notBefore(Instant.MIN)
            .subject("mock-test-subject");
    }

    public static Jwt user() {
        return jwt()
            .claim("sub", "mock-test-subject")
            .build();
    }
}
