package run.halo.app.security.filter;

import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.security.handler.AuthenticationFailureHandler;
import run.halo.app.security.handler.DefaultAuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
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

    private final AntPathMatcher antPathMatcher;

    private final HaloProperties haloProperties;

    protected AbstractAuthenticationFilter(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;

        antPathMatcher = new AntPathMatcher();
    }

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
}
