package run.halo.app.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.exception.NotInstallException;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.handler.AuthenticationFailureHandler;
import run.halo.app.security.handler.DefaultAuthenticationFailureHandler;
import run.halo.app.security.util.SecurityUtils;
import run.halo.app.service.OptionService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Abstract authentication filter.
 *
 * @author johnniang
 * @date 19-4-16
 */
@Slf4j
public abstract class AbstractAuthenticationFilter extends OncePerRequestFilter {

    protected final AntPathMatcher antPathMatcher;

    protected final HaloProperties haloProperties;

    protected final OptionService optionService;

    protected final StringCacheStore cacheStore;

    private AuthenticationFailureHandler failureHandler;
    /**
     * Exclude url patterns.
     */
    private Set<String> excludeUrlPatterns = new HashSet<>(2);

    AbstractAuthenticationFilter(HaloProperties haloProperties,
                                 OptionService optionService,
                                 StringCacheStore cacheStore) {
        this.haloProperties = haloProperties;
        this.optionService = optionService;
        this.cacheStore = cacheStore;

        antPathMatcher = new AntPathMatcher();
    }

    /**
     * Gets token from request.
     *
     * @param request http servlet request must not be null
     * @return token or null
     */
    @Nullable
    protected abstract String getTokenFromRequest(@NonNull HttpServletRequest request);

    protected abstract void doAuthenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        Assert.notNull(request, "Http servlet request must not be null");

        return excludeUrlPatterns.stream().anyMatch(p -> antPathMatcher.match(p, request.getServletPath()));
    }

    /**
     * Adds exclude url patterns.
     *
     * @param excludeUrlPatterns exclude urls
     */
    public void addExcludeUrlPatterns(@NonNull String... excludeUrlPatterns) {
        Assert.notNull(excludeUrlPatterns, "Exclude url patterns must not be null");

        Collections.addAll(this.excludeUrlPatterns, excludeUrlPatterns);
    }

    /**
     * Gets exclude url patterns.
     *
     * @return exclude url patterns.
     */
    @NonNull
    public Set<String> getExcludeUrlPatterns() {
        return excludeUrlPatterns;
    }

    /**
     * Sets exclude url patterns.
     *
     * @param excludeUrlPatterns exclude urls
     */
    public void setExcludeUrlPatterns(@NonNull Collection<String> excludeUrlPatterns) {
        Assert.notNull(excludeUrlPatterns, "Exclude url patterns must not be null");

        this.excludeUrlPatterns = new HashSet<>(excludeUrlPatterns);
    }

    /**
     * Gets authentication failure handler. (Default: @DefaultAuthenticationFailureHandler)
     *
     * @return authentication failure handler
     */
    @NonNull
    protected AuthenticationFailureHandler getFailureHandler() {
        if (failureHandler == null) {
            synchronized (this) {
                if (failureHandler == null) {
                    // Create default authentication failure handler
                    DefaultAuthenticationFailureHandler failureHandler = new DefaultAuthenticationFailureHandler();
                    failureHandler.setProductionEnv(haloProperties.isProductionEnv());

                    this.failureHandler = failureHandler;
                }
            }
        }
        return failureHandler;
    }

    /**
     * Sets authentication failure handler.
     *
     * @param failureHandler authentication failure handler
     */
    public void setFailureHandler(@NonNull AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "Authentication failure handler must not be null");

        this.failureHandler = failureHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Check whether the blog is installed or not
        Boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);

        if (!isInstalled) {
            // If not installed
            getFailureHandler().onFailure(request, response, new NotInstallException("当前博客还没有初始化"));
            return;
        }

        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (checkForTempToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Do authenticate
            doAuthenticate(request, response, filterChain);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean checkForTempToken(HttpServletRequest request) {
        // Get token from request
        String tempToken = getTokenFromRequest(request, HaloConst.TEMP_TOKEN, HaloConst.TEMP_TOKEN);

        if (StringUtils.isEmpty(tempToken)) {
            return false;
        }

        String tempTokenKey = SecurityUtils.buildTempTokenKey(tempToken);
        // Check the token
        Optional<Integer> tokenCountOptional = cacheStore.getAny(tempTokenKey, Integer.class);

        if (!tokenCountOptional.isPresent()) {
            // If the token is not found
            throw new ForbiddenException("The temporary token has been expired").setErrorData(tempToken);
        }

        log.info("Got valid temp token: [{}]", tempToken);

        int count = tokenCountOptional.get();
        // TODO May cause unsafe thread, fixing next time
        // Count down
        count--;
        if (count <= 0) {
            // If count is less than 0, then clear this temp token
            cacheStore.delete(tempTokenKey);
        } else {
            // Put the less count
            cacheStore.put(tempTokenKey, String.valueOf(count));
        }

        return true;
    }

    String getTokenFromRequest(@NonNull HttpServletRequest request, @NonNull String tokenQueryName, @NonNull String tokenHeaderName) {
        Assert.notNull(request, "Http servlet request must not be null");
        Assert.hasText(tokenQueryName, "Token query name must not be blank");
        Assert.hasText(tokenHeaderName, "Token header name must not be blank");

        // Get from header
        String accessKey = request.getHeader(tokenHeaderName);

        // Get from param
        if (StringUtils.isBlank(accessKey)) {
            accessKey = request.getParameter(tokenQueryName);

            log.debug("Got access key from parameter: [{}: {}]", tokenQueryName, accessKey);
        } else {
            log.debug("Got access key from header: [{}: {}]", tokenHeaderName, accessKey);
        }

        return accessKey;
    }

}
