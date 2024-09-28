package run.halo.app.security.authentication.exception;

import org.springframework.security.core.AuthenticationException;

public class TwoFactorAuthException extends AuthenticationException {

    public TwoFactorAuthException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TwoFactorAuthException(String msg) {
        super(msg);
    }

}
