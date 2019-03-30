package cc.ryanc.halo.security.filter;

import cc.ryanc.halo.cache.StringCacheStore;
import cc.ryanc.halo.config.properties.HaloProperties;
import cc.ryanc.halo.exception.AuthenticationException;
import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.security.authentication.AuthenticationImpl;
import cc.ryanc.halo.security.context.SecurityContextHolder;
import cc.ryanc.halo.security.context.SecurityContextImpl;
import cc.ryanc.halo.security.handler.AuthenticationFailureHandler;
import cc.ryanc.halo.security.handler.DefaultAuthenticationFailureHandler;
import cc.ryanc.halo.security.support.UserDetail;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

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

    private final HaloProperties haloProperties;

    private final StringCacheStore cacheStore;

    private final UserService userService;

    private final AntPathMatcher antPathMatcher;

    private Set<String> excludeUrlPatterns = new HashSet<>(1);

    public AdminAuthenticationFilter(StringCacheStore cacheStore,
                                     UserService userService,
                                     HaloProperties haloProperties) {
        this.cacheStore = cacheStore;
        this.userService = userService;
        this.haloProperties = haloProperties;
        antPathMatcher = new AntPathMatcher();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!haloProperties.getAuthEnabled()) {
            List<User> users = userService.listAll();
            if (!users.isEmpty()) {
                // Set security context
                User user = users.get(0);
                SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(new UserDetail(user))));
            }
            // If authentication disabled
            filterChain.doFilter(request, response);
            return;
        }

        // Get token from request
        String token = getTokenFromRequest(request);

        if (StringUtils.isNotBlank(token)) {

            // Valid the token
            Optional<String> userDetailOptional = cacheStore.get(token);

            if (!userDetailOptional.isPresent()) {
                getFailureHandler().onFailure(request, response, new AuthenticationException("The token has been expired or not exist").setErrorData(token));
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

        getFailureHandler().onFailure(request, response, new AuthenticationException("You have to login before accessing admin api"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return excludeUrlPatterns.stream().anyMatch(p -> antPathMatcher.match(p, request.getServletPath()));
    }

    /**
     * Gets authentication failure handler. (Default: @DefaultAuthenticationFailureHandler)
     *
     * @return authentication failure handler
     */
    public AuthenticationFailureHandler getFailureHandler() {
        if (failureHandler == null) {
            synchronized (this) {
                // Create default authentication failure handler
                failureHandler = new DefaultAuthenticationFailureHandler().setProductionEnv(haloProperties.getProductionEnv());
            }
        }
        return failureHandler;
    }

    /**
     * Sets authentication failure handler.
     *
     * @param failureHandler authentication failure handler
     */
    public AdminAuthenticationFilter setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    /**
     * Sets exclude url patterns.
     *
     * @param excludeUrlPatterns exclude urls
     */
    public AdminAuthenticationFilter setExcludeUrlPatterns(Collection<String> excludeUrlPatterns) {
        Assert.notNull(excludeUrlPatterns, "Exclude url patterns must not be null");

        this.excludeUrlPatterns = new HashSet<>(excludeUrlPatterns);
        return this;
    }

    /**
     * Adds exclude url patterns.
     *
     * @param excludeUrlPatterns exclude urls
     */
    public AdminAuthenticationFilter addExcludeUrlPatterns(String... excludeUrlPatterns) {
        Assert.notNull(excludeUrlPatterns, "Exclude url patterns must not be null");

        Collections.addAll(this.excludeUrlPatterns, excludeUrlPatterns);
        return this;
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
