package run.halo.app.security.authentication.twofactor.totp;

import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class TotpAuthenticationToken extends AbstractAuthenticationToken {

    private final int code;

    public TotpAuthenticationToken(int code) {
        super(List.of());
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public Object getCredentials() {
        return getCode();
    }

    @Override
    public Object getPrincipal() {
        return getCode();
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }
}
