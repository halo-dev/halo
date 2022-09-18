package run.halo.app.security.authentication.pat;

import java.time.Instant;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtPat implements Pat {

    private final Jwt jwt;

    public JwtPat(Jwt jwt) {
        this.jwt = jwt;
    }

    @Override
    public String getTokenValue() {
        return jwt.getTokenValue();
    }

    @Override
    public String getPatName() {
        return jwt.getClaimAsString("patName");
    }

    @Override
    public Instant getIssuedAt() {
        return jwt.getIssuedAt();
    }

    @Override
    public Instant getExpiresAt() {
        return jwt.getExpiresAt();
    }
}
