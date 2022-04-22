package run.halo.app.authentication.verifyer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import run.halo.app.identity.authentication.verifier.DefaultBearerTokenResolver;

/**
 * Tests for {@link DefaultBearerTokenResolver}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultBearerTokenResolverTest {
    private static final String CUSTOM_HEADER = "custom-header";
    private static final String TEST_TOKEN = "test-token";

    private DefaultBearerTokenResolver resolver;

    @BeforeEach
    public void setUp() {
        this.resolver = new DefaultBearerTokenResolver();
    }

    @Test
    public void resolveWhenValidHeaderIsPresentThenTokenIsResolved() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        assertThat(this.resolver.resolve(request)).isEqualTo(TEST_TOKEN);
    }

    @Test
    public void resolveWhenHeaderEndsWithPaddingIndicatorThenTokenIsResolved() {
        String token = TEST_TOKEN + "==";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        assertThat(this.resolver.resolve(request)).isEqualTo(token);
    }

    @Test
    public void resolveWhenCustomDefinedHeaderIsValidAndPresentThenTokenIsResolved() {
        this.resolver.setBearerTokenHeaderName(CUSTOM_HEADER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CUSTOM_HEADER, "Bearer " + TEST_TOKEN);
        assertThat(this.resolver.resolve(request)).isEqualTo(TEST_TOKEN);
    }

    @Test
    public void resolveWhenLowercaseHeaderIsPresentThenTokenIsResolved() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("authorization", "bearer " + TEST_TOKEN);
        assertThat(this.resolver.resolve(request)).isEqualTo(TEST_TOKEN);
    }

    @Test
    public void resolveWhenNoHeaderIsPresentThenTokenIsNotResolved() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(this.resolver.resolve(request)).isNull();
    }

    @Test
    public void resolveWhenHeaderWithWrongSchemeIsPresentThenTokenIsNotResolved() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization",
            "Basic " + Base64.getEncoder().encodeToString("test:test".getBytes()));
        assertThat(this.resolver.resolve(request)).isNull();
    }

    @Test
    @DisplayName("resolveWhenHeaderWithMissingTokenIsPresentThenAuthenticationExceptionIsThrown")
    public void resolveThenAuthenticationExceptionIsThrown() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");
        assertThatExceptionOfType(OAuth2AuthenticationException.class).isThrownBy(
                () -> this.resolver.resolve(request))
            .withMessageContaining(("Bearer token is malformed"));
    }

    @Test
    @DisplayName(
        "resolveWhenHeaderWithInvalidCharactersIsPresentThenAuthenticationExceptionIsThrown")
    public void resolveThenAuthenticationExceptionIsThrown2() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer an\"invalid\"token");
        assertThatExceptionOfType(OAuth2AuthenticationException.class).isThrownBy(
                () -> this.resolver.resolve(request))
            .withMessageContaining(("Bearer token is malformed"));
    }

    @Test
    @DisplayName("resolve when valid header is present together with "
        + "form parameter then AuthenticationException is thrown")
    public void resolveThenAuthenticationExceptionIsThrown3() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        request.setMethod("POST");
        request.setContentType("application/x-www-form-urlencoded");
        request.addParameter("access_token", TEST_TOKEN);
        assertThatExceptionOfType(OAuth2AuthenticationException.class).isThrownBy(
                () -> this.resolver.resolve(request))
            .withMessageContaining("Found multiple bearer tokens in the request");
    }

    @Test
    @DisplayName("resolve when valid header is present together with query"
        + " parameter then AuthenticationException is thrown")
    public void resolveThenAuthenticationExceptionIsThrown4() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        request.setMethod("GET");
        request.addParameter("access_token", TEST_TOKEN);
        assertThatExceptionOfType(OAuth2AuthenticationException.class).isThrownBy(
                () -> this.resolver.resolve(request))
            .withMessageContaining("Found multiple bearer tokens in the request");
    }

    @Test
    @DisplayName("resolve when request contains two access token query"
        + " parameters then AuthenticationException is thrown")
    public void resolveThenAuthenticationExceptionIsThrown5() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.addParameter("access_token", "token1", "token2");
        assertThatExceptionOfType(OAuth2AuthenticationException.class).isThrownBy(
                () -> this.resolver.resolve(request))
            .withMessageContaining("Found multiple bearer tokens in the request");
    }

    @Test
    @DisplayName("resolve when request contains two access token form parameters"
        + " then AuthenticationException is thrown")
    public void resolveThenAuthenticationExceptionIsThrown6() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("application/x-www-form-urlencoded");
        request.addParameter("access_token", "token1", "token2");
        assertThatExceptionOfType(OAuth2AuthenticationException.class).isThrownBy(
                () -> this.resolver.resolve(request))
            .withMessageContaining("Found multiple bearer tokens in the request");
    }

    @Test
    @DisplayName("resolve when parameter is present in multipart request"
        + " and form parameter supported then token is not resolved")
    public void resolveThenTokenIsNotResolved() {
        this.resolver.setAllowFormEncodedBodyParameter(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");
        request.addParameter("access_token", TEST_TOKEN);
        assertThat(this.resolver.resolve(request)).isNull();
    }

    @Test
    @DisplayName("resolveWhenFormParameterIsPresentAndSupportedThenTokenIsResolved")
    public void resolveThenTokenIsResolved() {
        this.resolver.setAllowFormEncodedBodyParameter(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("application/x-www-form-urlencoded");
        request.addParameter("access_token", TEST_TOKEN);
        assertThat(this.resolver.resolve(request)).isEqualTo(TEST_TOKEN);
    }

    @Test
    @DisplayName("resolveWhenFormParameterIsPresentAndNotSupportedThenTokenIsNotResolved")
    public void resolveThenTokenIsNotResolved2() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("application/x-www-form-urlencoded");
        request.addParameter("access_token", TEST_TOKEN);
        assertThat(this.resolver.resolve(request)).isNull();
    }

    @Test
    @DisplayName("resolveWhenQueryParameterIsPresentAndSupportedThenTokenIsResolved")
    public void resolveThenTokenIsResolved2() {
        this.resolver.setAllowUriQueryParameter(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.addParameter("access_token", TEST_TOKEN);
        assertThat(this.resolver.resolve(request)).isEqualTo(TEST_TOKEN);
    }

    @Test
    @DisplayName("resolveWhenQueryParameterIsPresentAndNotSupportedThenTokenIsNotResolved")
    public void resolveThenTokenIsNotResolved3() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.addParameter("access_token", TEST_TOKEN);
        assertThat(this.resolver.resolve(request)).isNull();
    }
}
