package run.halo.app.security.authentication.exception;

import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import run.halo.app.infra.exception.RateLimitExceededException;

/**
 * Too many requests exception while authenticating. Because
 * {@link RateLimitExceededException} is not a subclass of
 * {@link AuthenticationException}, we need to create a new exception class to map it.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class TooManyRequestsException extends AuthenticationException {

    public TooManyRequestsException(@Nullable Throwable throwable) {
        super("Too many requests.", throwable);
    }

}
