package run.halo.app.identity.authentication;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;

/**
 * @author guqing
 * @since 2.0.0
 */
public class JwtDaoAuthenticationProvider extends DaoAuthenticationProvider {
    private static final OAuth2TokenType PASSWORD_TOKEN = new OAuth2TokenType("password");
    private static final String ERROR_URI =
        "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final OAuth2AuthorizationService authorizationService;
    /**
     * TODO from token settings
     */
    private static final boolean isReuseRefreshTokens = false;

    public JwtDaoAuthenticationProvider(
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
        OAuth2AuthorizationService authorizationService) {
        this.tokenGenerator = tokenGenerator;
        this.authorizationService = authorizationService;
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal,
        Authentication authentication, UserDetails user) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            (UsernamePasswordAuthenticationToken) super.createSuccessAuthentication(principal,
                authentication, user);

        OAuth2Authorization authorization = this.authorizationService.findByUsername(
            usernamePasswordAuthenticationToken.getName(), PASSWORD_TOKEN);

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
            authorization.getRefreshToken();
        if (refreshToken == null || !refreshToken.isActive()) {
            // As per https://tools.ietf.org/html/rfc6749#section-5.2
            // invalid_grant: The provided authorization grant (e.g., authorization code,
            // resource owner credentials) or refresh token is invalid, expired, revoked [...].
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }

        Set<String> scopes = usernamePasswordAuthenticationToken.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .principal(authentication)
            .providerContext(ProviderContextHolder.getProviderContext())
            .authorizedScopes(scopes);

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization);

        // ----- Access token -----
        OAuth2TokenContext tokenContext =
            tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                "The token generator failed to generate the access token.", ERROR_URI);
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

        // ----- Refresh token -----
        OAuth2RefreshToken currentRefreshToken = refreshToken.getToken();
        if (!isReuseRefreshTokens) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
            if (generatedRefreshToken == null) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the refresh token.", ERROR_URI);
                throw new OAuth2AuthenticationException(error);
            }
            currentRefreshToken = new OAuth2RefreshToken(
                generatedRefreshToken.getTokenValue(), generatedRefreshToken.getIssuedAt(),
                generatedRefreshToken.getExpiresAt());
            authorizationBuilder.refreshToken(currentRefreshToken);
        }

        this.authorizationService.save(authorizationBuilder.build());

        return new OAuth2AccessTokenAuthenticationToken(authentication, accessToken,
            currentRefreshToken, Collections.emptyMap());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
