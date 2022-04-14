package run.halo.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import run.halo.app.identity.authentication.DefaultOAuth2TokenContext;
import run.halo.app.identity.authentication.JwtGenerator;
import run.halo.app.identity.authentication.OAuth2TokenContext;
import run.halo.app.identity.authentication.OAuth2TokenType;
import run.halo.app.identity.authentication.ProviderContext;
import run.halo.app.identity.authentication.ProviderSettings;

/**
 * Tests for {@link JwtGenerator}.
 *
 * @author guqing
 * @date 2022-04-14
 */
public class JwtGeneratorTest {
    private JwtEncoder jwtEncoder;
    private JwtGenerator jwtGenerator;
    private ProviderContext providerContext;

    @BeforeEach
    public void setUp() {
        this.jwtEncoder = mock(JwtEncoder.class);
        this.jwtGenerator = new JwtGenerator(this.jwtEncoder);
        ProviderSettings
            providerSettings = ProviderSettings.builder().issuer("https://provider.com").build();
        this.providerContext = new ProviderContext(providerSettings, null);
    }

    @Test
    public void constructorWhenJwtEncoderNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> new JwtGenerator(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("jwtEncoder cannot be null");
    }

    @Test
    public void generateWhenUnsupportedTokenTypeThenReturnNull() {
        // @formatter:off
        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
            .tokenType(new OAuth2TokenType("unsupported_token_type"))
            .build();
        // @formatter:on

        assertThat(this.jwtGenerator.generate(tokenContext)).isNull();
    }

    @Test
    public void generateWhenAccessTokenTypeThenReturnJwt() {
        TestingAuthenticationToken authentication =
            new TestingAuthenticationToken("userPrincipal", "123456", "ROLE_USER");
        Set<String> scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        // @formatter:off
        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
            .principal(authentication)
            .providerContext(this.providerContext)
            .authorizedScopes(scope)
            .tokenType(OAuth2TokenType.ACCESS_TOKEN)
            .build();
        // @formatter:on

        assertGeneratedTokenType(tokenContext);
    }

    private void assertGeneratedTokenType(OAuth2TokenContext tokenContext) {
        this.jwtGenerator.generate(tokenContext);

        ArgumentCaptor<JwtEncoderParameters> jwtEncoderParametersArgumentCaptor =
            ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(this.jwtEncoder).encode(jwtEncoderParametersArgumentCaptor.capture());

        JwtEncoderParameters encoderParameters = jwtEncoderParametersArgumentCaptor.getValue();
        JwsHeader jwsHeader = encoderParameters.getJwsHeader();
        assertThat(jwsHeader).isNotNull();
        assertThat(jwsHeader.getAlgorithm()).isEqualTo(SignatureAlgorithm.RS256);

        JwtClaimsSet jwtClaimsSet = encoderParameters.getClaims();
        assertThat(jwtClaimsSet.getIssuer().toExternalForm()).isEqualTo(
            tokenContext.getProviderContext().getIssuer());
        assertThat(jwtClaimsSet.getSubject()).isEqualTo(
            tokenContext.getPrincipal().getName());

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(30, ChronoUnit.MINUTES);
        assertThat(jwtClaimsSet.getIssuedAt()).isBetween(issuedAt.minusSeconds(1),
            issuedAt.plusSeconds(1));
        assertThat(jwtClaimsSet.getExpiresAt()).isBetween(expiresAt.minusSeconds(1),
            expiresAt.plusSeconds(1));

        if (tokenContext.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
            assertThat(jwtClaimsSet.getNotBefore()).isBetween(issuedAt.minusSeconds(1),
                issuedAt.plusSeconds(1));

            Set<String> scopes = jwtClaimsSet.getClaim(OAuth2ParameterNames.SCOPE);
            assertThat(scopes).isEqualTo(tokenContext.getAuthorizedScopes());
        }
    }
}
