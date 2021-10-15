package run.halo.app.security.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.security.handler.ContentAuthenticationFailureHandler;
import run.halo.app.security.service.OneTimeTokenService;
import run.halo.app.service.OptionService;
import run.halo.app.utils.HaloUtils;

/**
 * Content filter
 *
 * @author johnniang
 * @date 19-5-6
 */
@Component
@Order(-1)
public class ContentFilter extends AbstractAuthenticationFilter {

    public ContentFilter(HaloProperties haloProperties,
        OptionService optionService,
        AbstractStringCacheStore cacheStore,
        OneTimeTokenService oneTimeTokenService) {
        super(haloProperties, optionService, cacheStore, oneTimeTokenService);

        addUrlPatterns("/**");

        String adminPattern = HaloUtils.ensureBoth(haloProperties.getAdminPath(), "/") + "**";
        addExcludeUrlPatterns(
            adminPattern,
            "/api/**",
            "/install",
            "/version",
            "/js/**",
            "/css/**");

        // set failure handler
        setFailureHandler(new ContentAuthenticationFailureHandler());
    }

    @Override
    protected String getTokenFromRequest(HttpServletRequest request) {
        return null;
    }

    @Override
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // Do nothing
        // create session
        request.getSession(true);
        filterChain.doFilter(request, response);
    }
}
