package run.halo.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JoseHeaderNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import run.halo.app.identity.authentication.JwtGenerator;
import run.halo.app.identity.authentication.OAuth2AccessTokenAuthenticationToken;
import run.halo.app.identity.authentication.OAuth2Authorization;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authentication.OAuth2RefreshTokenAuthenticationProvider;
import run.halo.app.identity.authentication.OAuth2RefreshTokenAuthenticationToken;
import run.halo.app.identity.authentication.OAuth2TokenContext;
import run.halo.app.identity.authentication.OAuth2TokenGenerator;
import run.halo.app.identity.authentication.OAuth2TokenType;
import run.halo.app.identity.authentication.ProviderContext;
import run.halo.app.identity.authentication.ProviderContextHolder;
import run.halo.app.identity.authentication.ProviderSettings;

/**
 * Tests for {@link OAuth2RefreshTokenAuthenticationProvider}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class OAuth2RefreshTokenAuthenticationProviderTest {
    private OAuth2AuthorizationService authorizationService;
    private OAuth2TokenGenerator<?> tokenGenerator;
    private OAuth2RefreshTokenAuthenticationProvider authenticationProvider;

    @BeforeEach
    public void setUp() {
        this.authorizationService = mock(OAuth2AuthorizationService.class);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        when(jwtEncoder.encode(any())).thenReturn(createJwt(Collections.singleton("scope1")));
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        this.tokenGenerator = spy(new OAuth2TokenGenerator<OAuth2Token>() {
            @Override
            public OAuth2Token generate(OAuth2TokenContext context) {
                return jwtGenerator.generate(context);
            }
        });
        this.authenticationProvider = new OAuth2RefreshTokenAuthenticationProvider(
            this.authorizationService, this.tokenGenerator);
        ProviderSettings
            providerSettings = ProviderSettings.builder().issuer("https://provider.com").build();
        ProviderContextHolder.setProviderContext(new ProviderContext(providerSettings, null));
    }

    @AfterEach
    public void cleanup() {
        ProviderContextHolder.resetProviderContext();
    }

    private static Jwt createJwt(Set<String> scope) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(1, ChronoUnit.HOURS);
        return Jwt.withTokenValue("refreshed-access-token")
            .header(JoseHeaderNames.ALG, SignatureAlgorithm.RS256.getName())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim(OAuth2ParameterNames.SCOPE, scope)
            .build();
    }

    @Test
    public void constructorWhenAuthorizationServiceNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> new OAuth2RefreshTokenAuthenticationProvider(null, tokenGenerator))
            .isInstanceOf(IllegalArgumentException.class)
            .extracting(Throwable::getMessage)
            .isEqualTo("authorizationService cannot be null");
    }

    @Test
    public void constructorWhenJwtEncoderNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(
            () -> new OAuth2RefreshTokenAuthenticationProvider(this.authorizationService,
                null))
            .isInstanceOf(IllegalArgumentException.class)
            .extracting(Throwable::getMessage)
            .isEqualTo("tokenGenerator cannot be null");
    }

    @Test
    public void supportsWhenSupportedAuthenticationThenTrue() {
        assertThat(this.authenticationProvider.supports(
            OAuth2RefreshTokenAuthenticationToken.class)).isTrue();
    }

    @Test
    public void supportsWhenUnsupportedAuthenticationThenFalse() {
        assertThat(this.authenticationProvider.supports(
            UsernamePasswordAuthenticationToken.class)).isFalse();
    }

    @Test
    public void authenticateWhenValidRefreshTokenThenReturnAccessToken() {
        OAuth2Authorization authorization = TestOAuth2Authorizations.authorization().build();
        when(this.authorizationService.findByToken(
            eq(authorization.getRefreshToken().getToken().getTokenValue()),
            eq(OAuth2TokenType.REFRESH_TOKEN)))
            .thenReturn(authorization);
        String tokenValue = authorization.getRefreshToken().getToken().getTokenValue();
        OAuth2RefreshTokenAuthenticationToken authentication =
            new OAuth2RefreshTokenAuthenticationToken(
                tokenValue, null, null);
        OAuth2AccessTokenAuthenticationToken accessTokenAuthentication =
            (OAuth2AccessTokenAuthenticationToken) this.authenticationProvider.authenticate(
                authentication);

        ArgumentCaptor<OAuth2TokenContext> tokenContextArgumentCaptor =
            ArgumentCaptor.forClass(OAuth2TokenContext.class);
        verify(this.tokenGenerator, times(2)).generate(tokenContextArgumentCaptor.capture());
        List<OAuth2TokenContext> allValues = tokenContextArgumentCaptor.getAllValues();
        assertThat(allValues).isNotNull();
        // refresh token generate capture
        assertThat(allValues.size()).isEqualTo(2);
        OAuth2TokenContext tokenContext = allValues.get(1);
        assertThat(tokenContext.<Authentication>getPrincipal()).isEqualTo(
            authorization.getAttribute(
                Principal.class.getName()));
        assertThat(tokenContext.getAuthorizedScopes())
            .isEqualTo(
                authorization.getAttribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME));
        assertThat(tokenContext.getTokenType()).isEqualTo(OAuth2TokenType.REFRESH_TOKEN);

        ArgumentCaptor<OAuth2Authorization> authorizationCaptor =
            ArgumentCaptor.forClass(OAuth2Authorization.class);
        verify(this.authorizationService).save(authorizationCaptor.capture());
        OAuth2Authorization updatedAuthorization = authorizationCaptor.getValue();

        assertThat(accessTokenAuthentication.getAccessToken()).isEqualTo(
            updatedAuthorization.getAccessToken().getToken());
        assertThat(updatedAuthorization.getAccessToken()).isNotEqualTo(
            authorization.getAccessToken());
        assertThat(accessTokenAuthentication.getRefreshToken()).isEqualTo(
            updatedAuthorization.getRefreshToken().getToken());
        // By default, refresh token is opened
        assertThat(updatedAuthorization.getRefreshToken()).isNotEqualTo(
            authorization.getRefreshToken());
    }
}
