package run.halo.app.security.authentication.oauth2;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Mono;

/** Service for registering a Halo user from an unbound OAuth2 identity. */
public interface OAuth2RegistrationService {

    /**
     * Register a new user from the given OAuth2 token.
     *
     * @param token cached OAuth2 authentication token
     * @param agreedToTerms whether the user agreed to required agreement pages
     * @return registration result containing the created (or already bound) username
     */
    Mono<RegistrationResult> register(OAuth2AuthenticationToken token, boolean agreedToTerms);

    record RegistrationResult(String username, boolean needsEmailCompletion) {}
}
