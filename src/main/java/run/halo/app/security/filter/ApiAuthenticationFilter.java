package run.halo.app.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.properties.ApiProperties;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.service.OptionService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static run.halo.app.model.support.HaloConst.API_ACCESS_KEY_HEADER_NAME;
import static run.halo.app.model.support.HaloConst.API_ACCESS_KEY_QUERY_NAME;

/**
 * Api authentication Filter
 *
 * @author johnniang
 */
@Slf4j
public class ApiAuthenticationFilter extends AbstractAuthenticationFilter {

    private final OptionService optionService;

    public ApiAuthenticationFilter(HaloProperties haloProperties,
                                   OptionService optionService,
                                   StringCacheStore cacheStore) {
        super(haloProperties, optionService, cacheStore);
        this.optionService = optionService;
    }

    @Override
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!haloProperties.isAuthEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get api_enable from option
        Boolean apiEnabled = optionService.getByPropertyOrDefault(ApiProperties.API_ENABLED, Boolean.class, false);

        if (!apiEnabled) {
            getFailureHandler().onFailure(request, response, new ForbiddenException("API has been disabled by blogger currently"));
            return;
        }

        // Get access key
        String accessKey = getTokenFromRequest(request);

        if (StringUtils.isBlank(accessKey)) {
            // If the access key is missing
            getFailureHandler().onFailure(request, response, new AuthenticationException("Missing API access key"));
            return;
        }

        // Get access key from option
        Optional<String> optionalAccessKey = optionService.getByProperty(ApiProperties.API_ACCESS_KEY, String.class);

        if (!optionalAccessKey.isPresent()) {
            // If the access key is not set
            getFailureHandler().onFailure(request, response, new AuthenticationException("API access key hasn't been set by blogger"));
            return;
        }

        if (!StringUtils.equals(accessKey, optionalAccessKey.get())) {
            // If the access key is mismatch
            getFailureHandler().onFailure(request, response, new AuthenticationException("API access key is mismatch"));
            return;
        }

        // Do filter
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean result = super.shouldNotFilter(request);

        if (antPathMatcher.match("/api/content/*/comments", request.getServletPath())) {
            Boolean commentApiEnabled = optionService.getByPropertyOrDefault(CommentProperties.API_ENABLED, Boolean.class, true);
            if (!commentApiEnabled) {
                // If the comment api is disabled
                result = false;
            }
        }
        return result;
    }

    @Override
    protected String getTokenFromRequest(@NonNull HttpServletRequest request) {
        return getTokenFromRequest(request, API_ACCESS_KEY_QUERY_NAME, API_ACCESS_KEY_HEADER_NAME);
    }
}
