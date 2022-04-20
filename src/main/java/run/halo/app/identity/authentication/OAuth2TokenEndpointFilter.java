package run.halo.app.identity.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author guqing
 * @since 2.0.0
 */
public class OAuth2TokenEndpointFilter extends OncePerRequestFilter {
    /**
     * The default endpoint {@code URI} for access token requests.
     */
    private static final String DEFAULT_TOKEN_ENDPOINT_URI = "/api/v1/oauth2/token";

    private final AuthenticationManager authenticationManager;
    private final RequestMatcher tokenEndpointMatcher;
    private final HttpMessageConverter<OAuth2AccessTokenResponse> accessTokenHttpResponseConverter =
        new OAuth2AccessTokenResponseHttpMessageConverter();
    private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter =
        new OAuth2ErrorHttpMessageConverter();
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource =
        new WebAuthenticationDetailsSource();
    private AuthenticationConverter authenticationConverter;
    private AuthenticationSuccessHandler authenticationSuccessHandler =
        this::sendAccessTokenResponse;
    private AuthenticationFailureHandler authenticationFailureHandler = this::sendErrorResponse;

    /**
     * Constructs an {@code OAuth2TokenEndpointFilter} using the provided parameters.
     *
     * @param authenticationManager the authentication manager
     */
    public OAuth2TokenEndpointFilter(AuthenticationManager authenticationManager) {
        this(authenticationManager, DEFAULT_TOKEN_ENDPOINT_URI);
    }

    /**
     * Constructs an {@code OAuth2TokenEndpointFilter} using the provided parameters.
     *
     * @param authenticationManager the authentication manager
     * @param tokenEndpointUri the endpoint {@code URI} for access token requests
     */
    public OAuth2TokenEndpointFilter(AuthenticationManager authenticationManager,
        String tokenEndpointUri) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.hasText(tokenEndpointUri, "tokenEndpointUri cannot be empty");
        this.authenticationManager = authenticationManager;
        this.tokenEndpointMatcher =
            new AntPathRequestMatcher(tokenEndpointUri, HttpMethod.POST.name());
        this.authenticationConverter = new DelegatingAuthenticationConverter(
            List.of(new OAuth2RefreshTokenAuthenticationConverter(),
                new OAuth2PasswordAuthenticationConverter())
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

        if (!this.tokenEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String[] grantTypes = request.getParameterValues(OAuth2ParameterNames.GRANT_TYPE);
            if (grantTypes == null || grantTypes.length != 1) {
                OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST,
                    OAuth2ParameterNames.GRANT_TYPE, OAuth2EndpointUtils.ERROR_URI);
            }

            Authentication authorizationGrantAuthentication =
                this.authenticationConverter.convert(request);
            if (authorizationGrantAuthentication == null) {
                OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE,
                    OAuth2ParameterNames.GRANT_TYPE, OAuth2EndpointUtils.ERROR_URI);
            }
            if (authorizationGrantAuthentication instanceof AbstractAuthenticationToken) {
                ((AbstractAuthenticationToken) authorizationGrantAuthentication)
                    .setDetails(this.authenticationDetailsSource.buildDetails(request));
            }

            OAuth2AccessTokenAuthenticationToken accessTokenAuthentication =
                (OAuth2AccessTokenAuthenticationToken) this.authenticationManager.authenticate(
                    authorizationGrantAuthentication);
            this.authenticationSuccessHandler.onAuthenticationSuccess(request, response,
                accessTokenAuthentication);
        } catch (OAuth2AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }
    }

    /**
     * Sets the {@link AuthenticationDetailsSource} used for building an authentication details
     * instance from {@link HttpServletRequest}.
     *
     * @param authenticationDetailsSource the {@link AuthenticationDetailsSource} used for
     * building an authentication details instance from {@link HttpServletRequest}
     */
    public void setAuthenticationDetailsSource(
        AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "authenticationDetailsSource cannot be null");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    /**
     * Sets the {@link AuthenticationConverter} used when attempting to extract an Access Token
     * Request from {@link HttpServletRequest}
     * to an instance of {@link OAuth2AuthorizationGrantAuthenticationToken} used for
     * authenticating the authorization grant.
     *
     * @param authenticationConverter the {@link AuthenticationConverter} used when attempting to
     * extract an Access Token Request from {@link HttpServletRequest}
     */
    public void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
        Assert.notNull(authenticationConverter, "authenticationConverter cannot be null");
        this.authenticationConverter = authenticationConverter;
    }

    /**
     * Sets the {@link AuthenticationSuccessHandler} used for handling an
     * {@link OAuth2AccessTokenAuthenticationToken}
     * and returning the {@link OAuth2AccessTokenResponse Access Token Response}.
     *
     * @param authenticationSuccessHandler the {@link AuthenticationSuccessHandler} used for
     * handling an {@link OAuth2AccessTokenAuthenticationToken}
     */
    public void setAuthenticationSuccessHandler(
        AuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler cannot be null");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    /**
     * Sets the {@link AuthenticationFailureHandler} used for handling an
     * {@link OAuth2AuthenticationException}
     * and returning the {@link OAuth2Error Error Response}.
     *
     * @param authenticationFailureHandler the {@link AuthenticationFailureHandler} used for
     * handling an {@link OAuth2AuthenticationException}
     */
    public void setAuthenticationFailureHandler(
        AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    private void sendAccessTokenResponse(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        OAuth2AccessTokenAuthenticationToken accessTokenAuthentication =
            (OAuth2AccessTokenAuthenticationToken) authentication;

        OAuth2AccessToken accessToken = accessTokenAuthentication.getAccessToken();
        OAuth2RefreshToken refreshToken = accessTokenAuthentication.getRefreshToken();
        Map<String, Object> additionalParameters =
            accessTokenAuthentication.getAdditionalParameters();

        OAuth2AccessTokenResponse.Builder builder =
            OAuth2AccessTokenResponse.withToken(accessToken.getTokenValue())
                .tokenType(accessToken.getTokenType())
                .scopes(accessToken.getScopes());
        if (accessToken.getIssuedAt() != null && accessToken.getExpiresAt() != null) {
            builder.expiresIn(
                ChronoUnit.SECONDS.between(accessToken.getIssuedAt(), accessToken.getExpiresAt()));
        }
        if (refreshToken != null) {
            builder.refreshToken(refreshToken.getTokenValue());
        }
        if (!CollectionUtils.isEmpty(additionalParameters)) {
            builder.additionalParameters(additionalParameters);
        }
        OAuth2AccessTokenResponse accessTokenResponse = builder.build();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.accessTokenHttpResponseConverter.write(accessTokenResponse, null, httpResponse);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException {

        OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
        this.errorHttpResponseConverter.write(error, null, httpResponse);
    }
}
