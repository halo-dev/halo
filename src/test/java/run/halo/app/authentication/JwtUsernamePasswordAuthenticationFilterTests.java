package run.halo.app.authentication;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import run.halo.app.identity.authentication.JwtUsernamePasswordAuthenticationFilter;
import run.halo.app.identity.authentication.OAuth2AccessTokenAuthenticationToken;

/**
 * Tests for {@link JwtUsernamePasswordAuthenticationFilter}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class JwtUsernamePasswordAuthenticationFilterTests {
    private static final String DEFAULT_TOKEN_ENDPOINT_URI = "/api/v1/oauth2/login";
    private static final String REMOTE_ADDRESS = "remote-address";
    private AuthenticationManager authenticationManager;
    private JwtUsernamePasswordAuthenticationFilter filter;

    private final HttpMessageConverter<OAuth2AccessTokenResponse> accessTokenHttpResponseConverter =
        new OAuth2AccessTokenResponseHttpMessageConverter();

    @BeforeEach
    public void setUp() {
        this.authenticationManager = mock(AuthenticationManager.class);
        this.filter = new JwtUsernamePasswordAuthenticationFilter(this.authenticationManager);
    }

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void constructorWhenTokenEndpointUriNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(
            () -> new JwtUsernamePasswordAuthenticationFilter(null, this.authenticationManager))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("tokenEndpointUri cannot be empty.");
    }

    @Test
    public void setAuthenticationDetailsSourceWhenNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> this.filter.setAuthenticationDetailsSource(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("AuthenticationDetailsSource required");
    }

    @Test
    public void setAuthenticationSuccessHandlerWhenNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> this.filter.setAuthenticationSuccessHandler(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("successHandler cannot be null");
    }

    @Test
    public void setAuthenticationFailureHandlerWhenNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> this.filter.setAuthenticationFailureHandler(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("failureHandler cannot be null");
    }

    @Test
    public void doFilterWhenNotTokenRequestThenNotProcessed() throws Exception {
        String requestUri = "/path";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", requestUri);
        request.setServletPath(requestUri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        this.filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void doFilterWhenTokenRequestGetThenNotProcessed() throws Exception {
        String requestUri = DEFAULT_TOKEN_ENDPOINT_URI;
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        request.setServletPath(requestUri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        AuthenticationFailureHandler authenticationFailureHandler =
            mock(AuthenticationFailureHandler.class);
        filter.setAuthenticationFailureHandler(authenticationFailureHandler);

        this.filter.doFilter(request, response, filterChain);

        verify(authenticationFailureHandler).onAuthenticationFailure(any(HttpServletRequest.class),
            any(HttpServletResponse.class), any(AuthenticationException.class));

        verifyNoInteractions(filterChain);
    }

    @Test
    public void doFilterWhenAuthorizationCodeTokenRequestThenAccessTokenResponse()
        throws Exception {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, "token",
            Instant.now(), Instant.now().plus(Duration.ofHours(1)),
            new HashSet<>(Arrays.asList("scope1", "scope2")));
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
            "refresh-token", Instant.now(), Instant.now().plus(Duration.ofDays(1)));
        Authentication clientPrincipal =
            new UsernamePasswordAuthenticationToken("guqing", "123456");
        Map<String, Object>
            additionalParameters = Collections.singletonMap("custom-param", "custom-value");
        OAuth2AccessTokenAuthenticationToken accessTokenAuthentication =
            new OAuth2AccessTokenAuthenticationToken(clientPrincipal, accessToken, refreshToken,
                additionalParameters);

        when(this.authenticationManager.authenticate(any())).thenReturn(accessTokenAuthentication);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(clientPrincipal);
        SecurityContextHolder.setContext(securityContext);

        MockHttpServletRequest request = createClientTokenRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        this.filter.doFilter(request, response, filterChain);

        verifyNoInteractions(filterChain);

        ArgumentCaptor<UsernamePasswordAuthenticationToken>
            authorizationCodeAuthenticationCaptor =
            ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(this.authenticationManager).authenticate(
            authorizationCodeAuthenticationCaptor.capture());

        UsernamePasswordAuthenticationToken accessTokenAuthenticationToken =
            authorizationCodeAuthenticationCaptor.getValue();
        assertThat(accessTokenAuthenticationToken.getName()).isEqualTo("guqing");
        assertThat(accessTokenAuthenticationToken.getDetails())
            .asInstanceOf(type(WebAuthenticationDetails.class))
            .extracting(WebAuthenticationDetails::getRemoteAddress)
            .isEqualTo(REMOTE_ADDRESS);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        OAuth2AccessTokenResponse accessTokenResponse = readAccessTokenResponse(response);

        OAuth2AccessToken accessTokenResult = accessTokenResponse.getAccessToken();
        assertThat(accessTokenResult.getTokenType()).isEqualTo(accessToken.getTokenType());
        assertThat(accessTokenResult.getTokenValue()).isEqualTo(accessToken.getTokenValue());
        assertThat(accessTokenResult.getIssuedAt()).isBetween(
            accessToken.getIssuedAt().minusSeconds(1), accessToken.getIssuedAt().plusSeconds(1));
        assertThat(accessTokenResult.getExpiresAt()).isBetween(
            accessToken.getExpiresAt().minusSeconds(1), accessToken.getExpiresAt().plusSeconds(1));
        assertThat(accessTokenResult.getScopes()).isEqualTo(accessToken.getScopes());
        assertThat(accessTokenResponse.getRefreshToken().getTokenValue()).isEqualTo(
            refreshToken.getTokenValue());
        assertThat(accessTokenResponse.getAdditionalParameters()).containsExactly(
            entry("custom-param", "custom-value"));
    }

    @Test
    public void doFilterWhenCustomAuthenticationSuccessHandlerThenUsed() throws Exception {
        AuthenticationSuccessHandler authenticationSuccessHandler =
            mock(AuthenticationSuccessHandler.class);
        this.filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);

        Authentication clientPrincipal =
            new UsernamePasswordAuthenticationToken("guqing", "123456");
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, "token",
            Instant.now(), Instant.now().plus(Duration.ofHours(1)),
            new HashSet<>(Arrays.asList("scope1", "scope2")));
        OAuth2AccessTokenAuthenticationToken accessTokenAuthentication =
            new OAuth2AccessTokenAuthenticationToken(clientPrincipal, accessToken);

        when(this.authenticationManager.authenticate(any())).thenReturn(accessTokenAuthentication);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(clientPrincipal);
        SecurityContextHolder.setContext(securityContext);

        MockHttpServletRequest request = createClientTokenRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        this.filter.doFilter(request, response, filterChain);

        verify(authenticationSuccessHandler).onAuthenticationSuccess(any(), any(), any());
    }

    private OAuth2AccessTokenResponse readAccessTokenResponse(MockHttpServletResponse response)
        throws Exception {
        MockClientHttpResponse httpResponse = new MockClientHttpResponse(
            response.getContentAsByteArray(), HttpStatus.valueOf(response.getStatus()));
        return this.accessTokenHttpResponseConverter.read(OAuth2AccessTokenResponse.class,
            httpResponse);
    }

    private static MockHttpServletRequest createClientTokenRequest() {
        String requestUri = DEFAULT_TOKEN_ENDPOINT_URI;
        MockHttpServletRequest request = new MockHttpServletRequest("POST", requestUri);
        request.setServletPath(requestUri);
        request.setRemoteAddr(REMOTE_ADDRESS);
        request.addParameter("username", "guqing");
        request.addParameter("password", "123456");

        request.addParameter(OAuth2ParameterNames.GRANT_TYPE,
            AuthorizationGrantType.PASSWORD.getValue());
        request.addParameter("custom-param-1", "custom-value-1");

        return request;
    }
}
