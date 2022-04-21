package run.halo.app.identity.authentication.verifier;

/**
 * Standard error codes defined by the OAuth 2.0 Authorization Framework: Bearer Token
 * Usage.
 *
 * @author guqing
 * @see
 * <a href="https://tools.ietf.org/html/rfc6750#section-3.1">RFC 6750 Section 3.1: Error Codes</a>
 * @since 2.0.0
 */
public interface BearerTokenErrorCodes {
    /**
     * {@code invalid_request} - The request is missing a required parameter, includes an
     * unsupported parameter or parameter value, repeats the same parameter, uses more
     * than one method for including an access token, or is otherwise malformed.
     */
    String INVALID_REQUEST = "invalid_request";

    /**
     * {@code invalid_token} - The access token provided is expired, revoked, malformed,
     * or invalid for other reasons.
     */
    String INVALID_TOKEN = "invalid_token";

    /**
     * {@code insufficient_scope} - The request requires higher privileges than provided
     * by the access token.
     */
    String INSUFFICIENT_SCOPE = "insufficient_scope";
}
