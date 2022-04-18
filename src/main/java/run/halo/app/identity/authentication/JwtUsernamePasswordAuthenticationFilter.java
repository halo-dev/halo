package run.halo.app.identity.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Processes an authentication client request. Called
 * {@code AuthenticationProcessingFilter} prior to Spring Security 3.0.
 * <p>
 * Request parameter must present two parameters to this filter: a username and password. The
 * default parameter names to use are contained in the static fields
 * {@link #SPRING_SECURITY_FORM_USERNAME_KEY} and
 * {@link #SPRING_SECURITY_FORM_PASSWORD_KEY}. The parameter names can also be changed by
 * setting the {@code usernameParameter} and {@code passwordParameter} properties.
 * <p>
 * This filter by default responds to the URL {@code /api/v1/oauth2/login}.
 *
 * @author guqing
 * @see UsernamePasswordAuthenticationFilter
 * @see AbstractAuthenticationProcessingFilter
 * @see OAuth2AccessTokenAuthenticationToken
 * @since 2.0.0
 */
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    /**
     * The default endpoint {@code URI} for access token requests.
     */
    private static final String DEFAULT_TOKEN_ENDPOINT_URI = "/api/v1/oauth2/login";
    private final HttpMessageConverter<OAuth2AccessTokenResponse> accessTokenHttpResponseConverter =
        new OAuth2AccessTokenResponseHttpMessageConverter();
    private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter =
        new OAuth2ErrorHttpMessageConverter();

    private boolean postOnly = true;

    public JwtUsernamePasswordAuthenticationFilter() {
        this(DEFAULT_TOKEN_ENDPOINT_URI, null);
    }

    public JwtUsernamePasswordAuthenticationFilter(String defaultFilterProcessesUrl) {
        this(defaultFilterProcessesUrl, null);
    }

    public JwtUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        this(DEFAULT_TOKEN_ENDPOINT_URI, authenticationManager);
    }

    public JwtUsernamePasswordAuthenticationFilter(String defaultFilterProcessesUrl,
        AuthenticationManager authenticationManager) {
        super(authenticationManager);
        if (!StringUtils.hasText(defaultFilterProcessesUrl)) {
            throw new IllegalArgumentException("tokenEndpointUri cannot be empty.");
        }
        setFilterProcessesUrl(defaultFilterProcessesUrl);
        setAuthenticationSuccessHandler(this::sendAccessTokenResponse);
        setAuthenticationFailureHandler(this::sendErrorResponse);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response)
        throws AuthenticationException {

        if (this.postOnly && !HttpMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationServiceException(
                "Authentication method not supported: " + request.getMethod());
        }
        String username = obtainUsername(request);
        username = (username != null) ? username : "";
        username = username.trim();
        String password = obtainPassword(request);
        password = (password != null) ? password : "";
        UsernamePasswordAuthenticationToken authRequest =
            new UsernamePasswordAuthenticationToken(username, password);
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
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

        //OAuth2Error error = ((AuthenticationServiceException) exception).getMessage();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
        this.errorHttpResponseConverter.write(new OAuth2Error(exception.getMessage()), null,
            httpResponse);
    }

    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
        super.setPostOnly(postOnly);
    }
}
