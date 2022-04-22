package run.halo.app.identity.authentication;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.util.Assert;

/**
 * An {@link AuthenticationProvider} implementation for the OAuth 2.0 Password Grant.
 *
 * @author guqing
 * @see OAuth2PasswordAuthenticationProvider
 * @see OAuth2AuthorizationService
 * @see OAuth2TokenGenerator
 * @see
 * <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.3">Section-4.3 Password Credentials Grant</a>
 * @since 2.0.0
 */
public class OAuth2PasswordAuthenticationProvider extends DaoAuthenticationProvider
    implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public OAuth2PasswordAuthenticationProvider(
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
        OAuth2AuthorizationService authorizationService) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        OAuth2PasswordAuthenticationToken passwordAuthentication =
            (OAuth2PasswordAuthenticationToken) authentication;
        // Convert to UsernamePasswordAuthenticationToken type
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(passwordAuthentication.getUsername(),
                passwordAuthentication.getPassword());
        // and call the authenticate method of the super.
        // then super#authenticate() method will call this#createSuccessAuthentication()
        return super.authenticate(authenticationToken);
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal,
        Authentication authentication, UserDetails user) {
        // convert to UsernamePasswordAuthenticationToken
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            (UsernamePasswordAuthenticationToken) super.createSuccessAuthentication(principal,
                authentication, user);

        Set<String> scopes = usernamePasswordAuthenticationToken.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .principal(authentication)
            .providerContext(ProviderContextHolder.getProviderContext())
            .authorizedScopes(scopes);

        OAuth2Authorization.Builder authorizationBuilder = new OAuth2Authorization.Builder()
            .principalName(authentication.getName())
            .authorizationGrantType(AuthorizationGrantType.PASSWORD)
            .attribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME, scopes)
            .attribute(Principal.class.getName(), authentication);

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

        this.authorizationService.save(authorizationBuilder.build());

        return new OAuth2AccessTokenAuthenticationToken(authentication, accessToken,
            currentRefreshToken, Collections.emptyMap());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
