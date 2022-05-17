package run.halo.app.identity.authentication.verifier;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.log.LogMessage;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests that contain an OAuth 2.0
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>.
 * <p>
 * This filter should be wired with an {@link AuthenticationManager} that can authenticate
 * a {@link BearerTokenAuthenticationToken}.
 *
 * @author guqing
 * @see
 * <a href="https://tools.ietf.org/html/rfc6750">The OAuth 2.0 Authorization Framework: Bearer Token Usage</a>
 * @see JwtAuthenticationProvider
 * @since 2.0.0
 */
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    private AuthenticationEntryPoint authenticationEntryPoint =
        new BearerTokenAuthenticationEntryPoint();

    private AuthenticationFailureHandler authenticationFailureHandler =
        (request, response, exception) -> {
            if (exception instanceof AuthenticationServiceException) {
                throw exception;
            }
            this.authenticationEntryPoint.commence(request, response, exception);
        };

    private BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource =
        new WebAuthenticationDetailsSource();

    private SecurityContextRepository securityContextRepository =
        new NullSecurityContextRepository();

    /**
     * Construct a {@code BearerTokenAuthenticationFilter} using the provided parameter(s).
     *
     * @param authenticationManagerResolver authentication manager resolver
     */
    public BearerTokenAuthenticationFilter(
        AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) {
        Assert.notNull(authenticationManagerResolver,
            "authenticationManagerResolver cannot be null");
        this.authenticationManagerResolver = authenticationManagerResolver;
    }

    /**
     * Construct a {@code BearerTokenAuthenticationFilter} using the provided parameter(s).
     *
     * @param authenticationManager authentication manager
     */
    public BearerTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        this.authenticationManagerResolver = (request) -> authenticationManager;
    }

    /**
     * Extract any
     * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>
     * from the request and attempt an authentication.
     *
     * @param request http servlet request
     * @param response http servlet response
     * @param filterChain filter chain
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        String token;
        try {
            token = this.bearerTokenResolver.resolve(request);
        } catch (OAuth2AuthenticationException invalid) {
            this.logger.trace(
                "Sending to authentication entry point since failed to resolve bearer token",
                invalid);
            this.authenticationEntryPoint.commence(request, response, invalid);
            return;
        }
        if (token == null) {
            this.logger.trace("Did not process request since did not find bearer token");
            filterChain.doFilter(request, response);
            return;
        }

        BearerTokenAuthenticationToken authenticationRequest =
            new BearerTokenAuthenticationToken(token);
        authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

        try {
            AuthenticationManager authenticationManager =
                this.authenticationManagerResolver.resolve(request);
            Authentication authenticationResult =
                authenticationManager.authenticate(authenticationRequest);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationResult);
            SecurityContextHolder.setContext(context);
            this.securityContextRepository.saveContext(context, request, response);
            this.logger.debug(
                LogMessage.format("Set SecurityContextHolder to %s", authenticationResult));
            filterChain.doFilter(request, response);
        } catch (AuthenticationException failed) {
            SecurityContextHolder.clearContext();
            this.logger.trace("Failed to process authentication request", failed);
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
        }
    }

    /**
     * Sets the {@link SecurityContextRepository} to save the {@link SecurityContext} on
     * authentication success. The default action is not to save the
     * {@link SecurityContext}.
     *
     * @param securityContextRepository the {@link SecurityContextRepository} to use.
     * Cannot be null.
     */
    public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        Assert.notNull(securityContextRepository, "securityContextRepository cannot be null");
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Set the {@link BearerTokenResolver} to use. Defaults to
     * {@link DefaultBearerTokenResolver}.
     *
     * @param bearerTokenResolver the {@code BearerTokenResolver} to use
     */
    public void setBearerTokenResolver(BearerTokenResolver bearerTokenResolver) {
        Assert.notNull(bearerTokenResolver, "bearerTokenResolver cannot be null");
        this.bearerTokenResolver = bearerTokenResolver;
    }

    /**
     * Set the {@link AuthenticationEntryPoint} to use. Defaults to
     * {@link BearerTokenAuthenticationEntryPoint}.
     *
     * @param authenticationEntryPoint the {@code AuthenticationEntryPoint} to use
     */
    public void setAuthenticationEntryPoint(
        final AuthenticationEntryPoint authenticationEntryPoint) {
        Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * Set the {@link AuthenticationFailureHandler} to use. Default implementation invokes
     * {@link AuthenticationEntryPoint}.
     *
     * @param authenticationFailureHandler the {@code AuthenticationFailureHandler} to use
     * @since 5.2
     */
    public void setAuthenticationFailureHandler(
        final AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    /**
     * Set the {@link AuthenticationDetailsSource} to use. Defaults to
     * {@link WebAuthenticationDetailsSource}.
     *
     * @param authenticationDetailsSource the {@code AuthenticationConverter} to use
     * @since 5.5
     */
    public void setAuthenticationDetailsSource(
        AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "authenticationDetailsSource cannot be null");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }
}
