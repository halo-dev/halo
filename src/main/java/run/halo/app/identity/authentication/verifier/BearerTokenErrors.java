package run.halo.app.identity.authentication.verifier;

import org.springframework.http.HttpStatus;

/**
 * A factory for creating {@link BearerTokenError} instances that correspond to the
 * registered <a href="https://tools.ietf.org/html/rfc6750#section-3.1">Bearer Token Error Codes</a>
 *
 * @author guqing
 * @since 2.0.0
 */
public class BearerTokenErrors {
    private static final BearerTokenError DEFAULT_INVALID_REQUEST =
        invalidRequest("Invalid request");

    private static final BearerTokenError DEFAULT_INVALID_TOKEN = invalidToken("Invalid token");

    private static final BearerTokenError DEFAULT_INSUFFICIENT_SCOPE =
        insufficientScope("Insufficient scope", null);

    private static final String DEFAULT_URI = "https://tools.ietf.org/html/rfc6750#section-3.1";

    private BearerTokenErrors() {
    }

    /**
     * Create a {@link BearerTokenError} caused by an invalid request
     *
     * @param message a description of the error
     * @return a {@link BearerTokenError}
     */
    public static BearerTokenError invalidRequest(String message) {
        try {
            return new BearerTokenError(BearerTokenErrorCodes.INVALID_REQUEST,
                HttpStatus.BAD_REQUEST, message,
                DEFAULT_URI);
        } catch (IllegalArgumentException ex) {
            // some third-party library error messages are not suitable for RFC 6750's
            // error message charset
            return DEFAULT_INVALID_REQUEST;
        }
    }

    /**
     * Create a {@link BearerTokenError} caused by an invalid token
     *
     * @param message a description of the error
     * @return a {@link BearerTokenError}
     */
    public static BearerTokenError invalidToken(String message) {
        try {
            return new BearerTokenError(BearerTokenErrorCodes.INVALID_TOKEN,
                HttpStatus.UNAUTHORIZED, message,
                DEFAULT_URI);
        } catch (IllegalArgumentException ex) {
            // some third-party library error messages are not suitable for RFC 6750's
            // error message charset
            return DEFAULT_INVALID_TOKEN;
        }
    }

    /**
     * Create a {@link BearerTokenError} caused by an invalid token
     *
     * @param scope the scope attribute to use in the error
     * @return a {@link BearerTokenError}
     */
    public static BearerTokenError insufficientScope(String message, String scope) {
        try {
            return new BearerTokenError(BearerTokenErrorCodes.INSUFFICIENT_SCOPE,
                HttpStatus.FORBIDDEN, message,
                DEFAULT_URI, scope);
        } catch (IllegalArgumentException ex) {
            // some third-party library error messages are not suitable for RFC 6750's
            // error message charset
            return DEFAULT_INSUFFICIENT_SCOPE;
        }
    }
}
