package run.halo.app.identity.authentication;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.util.Assert;

/**
 * An {@link AuthenticationProvider} implementation for the OAuth 2.0 Refresh Token Grant.
 *
 * @author guqing
 * @see OAuth2RefreshTokenAuthenticationToken
 * @see OAuth2AccessTokenAuthenticationToken
 * @see OAuth2AuthorizationService
 * @see OAuth2TokenGenerator
 * @see
 * <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-1.5">Section 1.5 Refresh Token Grant</a>
 * @see
 * <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-6">Section 6 Refreshing an Access Token</a>
 * @since 2.0.0
 */
public class OAuth2RefreshTokenAuthenticationProvider implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    /**
     * Constructs an {@code OAuth2RefreshTokenAuthenticationProvider} using the provided parameters.
     *
     * @param authorizationService the authorization service
     * @param tokenGenerator the token generator
     * @since 0.2.3
     */
    public OAuth2RefreshTokenAuthenticationProvider(OAuth2AuthorizationService authorizationService,
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws
        AuthenticationException {
        OAuth2RefreshTokenAuthenticationToken refreshTokenAuthentication =
            (OAuth2RefreshTokenAuthenticationToken) authentication;

        if (!refreshTokenAuthentication.isAuthenticated()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        OAuth2Authorization authorization = this.authorizationService.findByToken(
            refreshTokenAuthentication.getRefreshToken(), OAuth2TokenType.REFRESH_TOKEN);
        if (authorization == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
            authorization.getRefreshToken();
        if (refreshToken == null || !refreshToken.isActive()) {
            // As per https://tools.ietf.org/html/rfc6749#section-5.2
            // invalid_grant: The provided authorization grant (e.g., authorization code,
            // resource owner credentials) or refresh token is invalid, expired, revoked [...].
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }

        // As per https://tools.ietf.org/html/rfc6749#section-6
        // The requested scope MUST NOT include any scope not originally granted by the resource
        // owner,
        // and if omitted is treated as equal to the scope originally granted by the resource owner.
        Set<String> scopes = refreshTokenAuthentication.getScopes();
        Set<String> authorizedScopes =
            authorization.getAttribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME);
        if (!authorizedScopes.containsAll(scopes)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
        }
        if (scopes.isEmpty()) {
            scopes = authorizedScopes;
        }

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .principal(authorization.getAttribute(Principal.class.getName()))
            .providerContext(ProviderContextHolder.getProviderContext())
            .authorizedScopes(scopes);

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization);

        // ----- Access token -----
        OAuth2TokenContext tokenContext =
            tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                "The token generator failed to generate the access token.",
                OAuth2EndpointUtils.ERROR_URI);
            throw new OAuth2AuthenticationException(error);
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
            generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
            generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
        if (generatedAccessToken instanceof ClaimAccessor) {
            authorizationBuilder.token(accessToken, (metadata) -> {
                metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
                    ((ClaimAccessor) generatedAccessToken).getClaims());
                metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, false);
            });
        } else {
            authorizationBuilder.accessToken(accessToken);
        }

        ProviderSettings providerSettings =
            ProviderContextHolder.getProviderContext().providerSettings();

        // ----- Refresh token -----
        OAuth2RefreshToken currentRefreshToken = null;
        if (!providerSettings.isReuseRefreshTokens()) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
            if (generatedRefreshToken == null) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the refresh token.",
                    OAuth2EndpointUtils.ERROR_URI);
                throw new OAuth2AuthenticationException(error);
            }
            currentRefreshToken = new OAuth2RefreshToken(
                generatedRefreshToken.getTokenValue(), generatedRefreshToken.getIssuedAt(),
                generatedRefreshToken.getExpiresAt());
            authorizationBuilder.refreshToken(currentRefreshToken);
        }

        authorization = authorizationBuilder.build();

        this.authorizationService.save(authorization);

        return new OAuth2AccessTokenAuthenticationToken(refreshTokenAuthentication, accessToken,
            currentRefreshToken, Collections.emptyMap());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2RefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
