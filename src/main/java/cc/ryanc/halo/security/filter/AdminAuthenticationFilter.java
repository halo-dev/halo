package cc.ryanc.halo.security.filter;

import cc.ryanc.halo.cache.StringCacheStore;
import cc.ryanc.halo.exception.AuthenticationException;
import cc.ryanc.halo.security.authentication.AuthenticationImpl;
import cc.ryanc.halo.security.context.SecurityContextHolder;
import cc.ryanc.halo.security.context.SecurityContextImpl;
import cc.ryanc.halo.security.handler.AuthenticationFailureHandler;
import cc.ryanc.halo.security.support.UserDetail;
import cc.ryanc.halo.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Admin authentication filter.
 *
 * @author johnniang
 */
public class AdminAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Admin session key.
     */
    public final static String ADMIN_SESSION_KEY = "halo.admin.session";

    /**
     * Admin token header name.
     */
    public final static String ADMIN_TOKEN_HEADER_NAME = "ADMIN-" + HttpHeaders.AUTHORIZATION;

    /**
     * Admin token param name.
     */
    public final static String ADMIN_TOKEN_PARAM_NAME = "adminToken";

    private AuthenticationFailureHandler failureHandler;

    private final StringCacheStore cacheStore;

    public AdminAuthenticationFilter(StringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Get token from request
        String token = getTokenFromRequest(request);

        if (StringUtils.isNotBlank(token)) {

            // Valid the token
            Optional<String> userDetailOptional = cacheStore.get(token);

            if (!userDetailOptional.isPresent()) {
                failureHandler.onFailure(request, response, new AuthenticationException("The token has been expired or not exist").setErrorData(token));
                return;
            }

            UserDetail userDetail = JsonUtils.jsonToObject(userDetailOptional.get(), UserDetail.class);

            // Set security
            SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(userDetail)));

            filterChain.doFilter(request, response);
            return;
        }

        // Get info from session
        Object adminSessionValue = request.getSession().getAttribute(ADMIN_SESSION_KEY);

        if (adminSessionValue instanceof UserDetail) {
            // Convert to user detail
            UserDetail userDetail = (UserDetail) adminSessionValue;

            // Set security context
            SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(userDetail)));

            filterChain.doFilter(request, response);
            return;
        }

        failureHandler.onFailure(request, response, new AuthenticationException("You have to login before accessing admin api"));
    }

    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    /**
     * Gets token from request.
     *
     * @param request http servlet request must not be null
     * @return token or null
     */
    @Nullable
    private String getTokenFromRequest(@NonNull HttpServletRequest request) {
        Assert.notNull(request, "Http servlet request must not be null");

        // Get from header
        String token = request.getHeader(ADMIN_TOKEN_HEADER_NAME);

        // Get from param
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(ADMIN_TOKEN_PARAM_NAME);
        }

        return token;
    }
}
