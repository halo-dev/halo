package run.halo.app.identity.authentication.verifier;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

/**
 * A strategy for resolving
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>
 * s from the {@link HttpServletRequest}.
 *
 * @author guqing
 * @see
 * <a href="https://tools.ietf.org/html/rfc6750#section-2">RFC 6750 Section 2: Authenticated Requests</a>
 * @since 2.0.0
 */
@FunctionalInterface
public interface BearerTokenResolver {
    /**
     * Resolve any
     * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>
     * value from the request.
     *
     * @param request the request
     * @return the Bearer Token value or {@code null} if none found
     * @throws OAuth2AuthenticationException if the found token is invalid
     */
    String resolve(HttpServletRequest request);

}
