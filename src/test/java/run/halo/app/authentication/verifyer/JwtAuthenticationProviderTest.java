package run.halo.app.authentication.verifyer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Objects;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import run.halo.app.identity.authentication.verifier.BearerTokenAuthenticationToken;
import run.halo.app.identity.authentication.verifier.BearerTokenErrorCodes;
import run.halo.app.identity.authentication.verifier.JwtAuthenticationProvider;
import run.halo.app.identity.authentication.verifier.JwtAuthenticationToken;

/**
 * Tests for {@link JwtAuthenticationProvider}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationProviderTest {
    @Mock
    Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter;

    @Mock
    JwtDecoder jwtDecoder;

    JwtAuthenticationProvider provider;

    @BeforeEach
    public void setup() {
        this.provider = new JwtAuthenticationProvider(this.jwtDecoder);
        this.provider.setJwtAuthenticationConverter(this.jwtAuthenticationConverter);
    }

    @Test
    @DisplayName("authenticate when jwt decodes then authentication has attributes contained in "
        + "Jwt")
    public void authenticateThenAttributesContainedInJwt() {
        BearerTokenAuthenticationToken token = this.authentication();
        Jwt jwt = TestJwts.jwt().claim("name", "value").build();
        given(this.jwtDecoder.decode("token")).willReturn(jwt);
        given(this.jwtAuthenticationConverter.convert(jwt)).willReturn(
            new JwtAuthenticationToken(jwt));
        JwtAuthenticationToken authentication =
            (JwtAuthenticationToken) this.provider.authenticate(token);
        assertThat(authentication.getTokenAttributes()).containsEntry("name", "value");
    }

    @Test
    public void authenticateWhenJwtDecodeFailsThenRespondsWithInvalidToken() {
        BearerTokenAuthenticationToken token = this.authentication();
        given(this.jwtDecoder.decode("token")).willThrow(BadJwtException.class);
        assertThatExceptionOfType(OAuth2AuthenticationException.class)
            .isThrownBy(() -> this.provider.authenticate(token))
            .matches(errorCode(BearerTokenErrorCodes.INVALID_TOKEN));
    }

    @Test
    @DisplayName("authenticate when decoder throws incompatible error message then wraps with "
        + "generic one")
    public void authenticateWrapsWithGenericOne() {
        BearerTokenAuthenticationToken token = this.authentication();
        given(this.jwtDecoder.decode(token.getToken())).willThrow(
            new BadJwtException("with \"invalid\" chars"));
        assertThatExceptionOfType(OAuth2AuthenticationException.class)
            .isThrownBy(() -> this.provider.authenticate(token))
            .satisfies((ex) -> assertThat(ex).hasFieldOrPropertyWithValue("error.description",
                "Invalid token"));
    }

    @Test
    public void authenticateWhenDecoderFailsGenericallyThenThrowsGenericException() {
        BearerTokenAuthenticationToken token = this.authentication();
        given(this.jwtDecoder.decode(token.getToken()))
            .willThrow(new JwtException("no jwk set"));
        assertThatExceptionOfType(AuthenticationException.class)
            .isThrownBy(() -> this.provider.authenticate(token))
            .isNotInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    @DisplayName("authenticate when converter returns authentication then provider propagates it")
    public void authenticateProviderPropagatesIt() {
        BearerTokenAuthenticationToken token = this.authentication();
        Object details = mock(Object.class);
        token.setDetails(details);
        Jwt jwt = TestJwts.jwt().build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        given(this.jwtDecoder.decode(token.getToken())).willReturn(jwt);
        given(this.jwtAuthenticationConverter.convert(jwt)).willReturn(authentication);
        assertThat(this.provider.authenticate(token))
            .isEqualTo(authentication).hasFieldOrPropertyWithValue("details",
                details);
    }

    @Test
    public void supportsWhenBearerTokenAuthenticationTokenThenReturnsTrue() {
        assertThat(this.provider.supports(BearerTokenAuthenticationToken.class)).isTrue();
    }

    @Test
    @DisplayName("unSupports when BearerTokenAuthenticationToken then returns true")
    public void unSupportsJwtAuthenticationToken() {
        assertThat(this.provider.supports(JwtAuthenticationToken.class)).isFalse();
    }

    private BearerTokenAuthenticationToken authentication() {
        return new BearerTokenAuthenticationToken("token");
    }

    private Predicate<? super Throwable> errorCode(String errorCode) {
        return (failed) -> Objects.equals(
            ((OAuth2AuthenticationException) failed).getError().getErrorCode(), errorCode);
    }
}
