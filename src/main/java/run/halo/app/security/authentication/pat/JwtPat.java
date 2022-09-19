package run.halo.app.security.authentication.pat;

import java.time.Instant;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtPat implements Pat {

    private final Jwt jwt;

    private final String tokenValue;

    public JwtPat(Jwt jwt, String tokenValue) {
        this.jwt = jwt;
        this.tokenValue = tokenValue;
    }

    @Override
    public String getTokenValue() {
        return tokenValue;
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
