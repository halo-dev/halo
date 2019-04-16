package run.halo.app.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.model.entity.User;
import run.halo.app.security.authentication.AuthenticationImpl;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.context.SecurityContextImpl;
import run.halo.app.security.support.UserDetail;
import run.halo.app.service.UserService;
import run.halo.app.utils.JsonUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Admin authentication filter.
 *
 * @author johnniang
 */
@Slf4j
public class AdminAuthenticationFilter extends AbstractAuthenticationFilter {

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

    private final HaloProperties haloProperties;

    private final StringCacheStore cacheStore;

    private final UserService userService;

    public AdminAuthenticationFilter(StringCacheStore cacheStore,
                                     UserService userService,
                                     HaloProperties haloProperties) {
        super(haloProperties);
        this.cacheStore = cacheStore;
        this.userService = userService;
        this.haloProperties = haloProperties;
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
            // TODO Add key prefix
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

        if (shouldSkipAuthenticateFailure(request)) {
            // If should skip this authentication failure
            log.debug("Skipping authentication failure, url: [{}], method: [{}]", request.getServletPath(), request.getMethod());
            filterChain.doFilter(request, response);
            return;
        }

        getFailureHandler().onFailure(request, response, new AuthenticationException("You have to login before accessing admin api"));
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
