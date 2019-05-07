package run.halo.app.security.filter;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.NotInstallException;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.handler.AuthenticationFailureHandler;
import run.halo.app.security.handler.DefaultAuthenticationFailureHandler;
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
public abstract class AbstractAuthenticationFilter extends OncePerRequestFilter {

    private AuthenticationFailureHandler failureHandler;

    /**
     * Exclude url patterns.
     */
    private Set<String> excludeUrlPatterns = new HashSet<>(2);

    /**
     * Try authenticating url, method patterns.
     */
    private Map<String, String> tryAuthUrlMethodPatterns = new HashMap<>(2);

    protected final AntPathMatcher antPathMatcher;

    protected final HaloProperties haloProperties;

    protected final OptionService optionService;

    protected AbstractAuthenticationFilter(HaloProperties haloProperties,
                                           OptionService optionService) {
        this.haloProperties = haloProperties;
        this.optionService = optionService;

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
     * Should skip authentication failure.
     *
     * @param request http servlet request must not be null.
     * @return true if the request should skip authentication failure; false otherwise
     */
    @Deprecated
    protected boolean shouldSkipAuthenticateFailure(@NonNull HttpServletRequest request) {
        Assert.notNull(request, "Http servlet request must not be null");

        for (String url : tryAuthUrlMethodPatterns.keySet()) {
            if (antPathMatcher.match(url, request.getServletPath())
                    && tryAuthUrlMethodPatterns.get(url).equalsIgnoreCase(request.getMethod())) {
                return true;
            }
        }

        return false;
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
     * Adds try authenticating url method pattern.
     *
     * @param url    url must not be blank
     * @param method method must not be blank
     */
    @Deprecated
    public void addTryAuthUrlMethodPattern(@NonNull String url, @NonNull String method) {
        Assert.hasText(url, "Try authenticating url must not be blank");
        Assert.hasText(method, "Try authenticating method must not be blank");

        tryAuthUrlMethodPatterns.put(url, method);
    }


    /**
     * Gets authentication failure handler. (Default: @DefaultAuthenticationFailureHandler)
     *
     * @return authentication failure handler
     */
    @NonNull
    public AuthenticationFailureHandler getFailureHandler() {
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
            getFailureHandler().onFailure(request, response, new NotInstallException("The blog has not been initialized yet!"));
            return;
        }

        if (shouldNotFilter(request)) {
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

}
