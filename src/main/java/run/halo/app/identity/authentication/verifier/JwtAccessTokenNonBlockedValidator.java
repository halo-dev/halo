package run.halo.app.identity.authentication.verifier;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import run.halo.app.identity.authentication.OAuth2Authorization;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authentication.OAuth2TokenType;

/**
 * <p>An implementation of {@link OAuth2TokenValidator} for verifying a Jwt-based access token has
 * none blocked.</p>
 * <p>Because the persistent access token will be manually removed or logged out,
 * this token should not continue to be used in this case.</p>
 *
 * @author guqing
 * @see OAuth2TokenValidator
 * @see OAuth2AuthorizationService
 * @since 2.0.0
 */
public class JwtAccessTokenNonBlockedValidator implements OAuth2TokenValidator<Jwt> {

    private final OAuth2AuthorizationService oauth2AuthorizationService;
    private final OAuth2Error error;

    public JwtAccessTokenNonBlockedValidator(
        OAuth2AuthorizationService oauth2AuthorizationService) {
        this.oauth2AuthorizationService = oauth2AuthorizationService;
        this.error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
            "The access token is not valid",
            "https://tools.ietf.org/html/rfc6750#section-3.1");
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String tokenValue = token.getTokenValue();
        if (tokenValue == null) {
            return OAuth2TokenValidatorResult.failure(this.error);
        }
        OAuth2Authorization oauth2Authorization =
            oauth2AuthorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
        if (oauth2Authorization == null) {
            return OAuth2TokenValidatorResult.failure(this.error);
        }

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
            oauth2Authorization.getAccessToken();
        if (accessToken == null || accessToken.isExpired()) {
            return OAuth2TokenValidatorResult.failure(this.error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
