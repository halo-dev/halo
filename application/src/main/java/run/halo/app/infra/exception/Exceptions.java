package run.halo.app.infra.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.Map;
import org.springframework.security.authentication.BadCredentialsException;

public enum Exceptions {
    ;

    public static final String DEFAULT_TYPE = "about:blank";

    public static final String THEME_ALREADY_EXISTS_TYPE =
        "https://halo.run/probs/theme-alreay-exists";

    public static final String INVALID_CREDENTIAL_TYPE =
        "https://halo.run/probs/invalid-credential";

    public static final String REQUEST_NOT_PERMITTED_TYPE =
        "https://halo.run/probs/request-not-permitted";

    /**
     * Non-ErrorResponse exception to type map.
     */
    public static final Map<Class<? extends Throwable>, String> EXCEPTION_TYPE_MAP = Map.of(
        RequestNotPermitted.class, REQUEST_NOT_PERMITTED_TYPE,
        BadCredentialsException.class, INVALID_CREDENTIAL_TYPE
    );
}
