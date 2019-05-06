package run.halo.app.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.model.entity.User;
import run.halo.app.security.authentication.AuthenticationImpl;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.context.SecurityContextImpl;
import run.halo.app.security.support.UserDetail;
import run.halo.app.security.util.SecurityUtils;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;

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
@Slf4j
public class AdminAuthenticationFilter extends AbstractAuthenticationFilter {

    /**
     * Admin session key.
     */
    public final static String ADMIN_SESSION_KEY = "halo.admin.session";

    /**
     * Access token cache prefix.
     */
    public final static String TOKEN_ACCESS_CACHE_PREFIX = "halo.admin.access.token.";

    /**
     * Refresh token cache prefix.
     */
    public final static String TOKEN_REFRESH_CACHE_PREFIX = "halo.admin.refresh.token.";

    /**
     * Admin token header name.
     */
    public final static String ADMIN_TOKEN_HEADER_NAME = "ADMIN-" + HttpHeaders.AUTHORIZATION;

    /**
     * Admin token param name.
     */
    public final static String ADMIN_TOKEN_QUERY_NAME = "admin_token";

    private final HaloProperties haloProperties;

    private final StringCacheStore cacheStore;

    private final UserService userService;

    public AdminAuthenticationFilter(StringCacheStore cacheStore,
                                     UserService userService,
                                     HaloProperties haloProperties,
                                     OptionService optionService) {
        super(haloProperties, optionService);
        this.cacheStore = cacheStore;
        this.userService = userService;
        this.haloProperties = haloProperties;
    }

    @Override
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!haloProperties.isAuthEnabled()) {
            // Set security
            userService.getCurrentUser().ifPresent(user ->
                    SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(new UserDetail(user)))));

            // Do filter
            filterChain.doFilter(request, response);
            return;
        }

        // Get token from request
        String token = getTokenFromRequest(request);

        if (StringUtils.isBlank(token)) {
            getFailureHandler().onFailure(request, response, new AuthenticationException("You have to login before accessing admin api"));
            return;
        }

        // Get user id from cache
        Optional<Integer> optionalUserId = cacheStore.getAny(SecurityUtils.buildTokenAccessKey(token), Integer.class);

        if (!optionalUserId.isPresent()) {
            getFailureHandler().onFailure(request, response, new AuthenticationException("The token has been expired or not exist").setErrorData(token));
            return;
        }

        // Get the user
        User user = userService.getById(optionalUserId.get());

        // Build user detail
        UserDetail userDetail = new UserDetail(user);

        // Set security
        SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(userDetail)));

        // Do filter
        filterChain.doFilter(request, response);
    }

    @Override
    protected String getTokenFromRequest(@NonNull HttpServletRequest request) {
        Assert.notNull(request, "Http servlet request must not be null");

        // Get from header
        String token = request.getHeader(ADMIN_TOKEN_HEADER_NAME);

        // Get from param
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(ADMIN_TOKEN_QUERY_NAME);

            log.debug("Got token from parameter: [{}: {}]", ADMIN_TOKEN_QUERY_NAME, token);
        } else {
            log.debug("Got token from header: [{}: {}]", ADMIN_TOKEN_HEADER_NAME, token);
        }

        return token;
    }

}
