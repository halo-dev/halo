package run.halo.app.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.model.properties.OtherProperties;
import run.halo.app.service.OptionService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Api authentication Filter
 *
 * @author johnniang
 */
@Slf4j
public class ApiAuthenticationFilter extends AbstractAuthenticationFilter {

    public final static String API_TOKEN_HEADER_NAME = "API-" + HttpHeaders.AUTHORIZATION;

    public final static String API_TOKEN_QUERY_NAME = "api_token";

    private final OptionService optionService;

    public ApiAuthenticationFilter(HaloProperties haloProperties,
                                   OptionService optionService) {
        super(haloProperties, optionService);
        this.optionService = optionService;
    }

    @Override
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Get token
        String token = getTokenFromRequest(request);

        if (StringUtils.isBlank(token)) {
            // If the token is missing
            getFailureHandler().onFailure(request, response, new AuthenticationException("Missing API token"));
            return;
        }

        // Get api_enable from option
        Boolean apiEnabled = optionService.getByPropertyOrDefault(OtherProperties.API_ENABLED, Boolean.class, false);

        if (!apiEnabled) {
            getFailureHandler().onFailure(request, response, new ForbiddenException("API has been disabled by blogger currently"));
            return;
        }

        // Get token from option
        Optional<String> optionalToken = optionService.getByProperty(OtherProperties.API_TOKEN, String.class);

        if (!optionalToken.isPresent()) {
            // If the token is not set
            getFailureHandler().onFailure(request, response, new AuthenticationException("API Token hasn't been set by blogger"));
            return;
        }

        if (!StringUtils.equals(token, optionalToken.get())) {
            // If the token is mismatch
            getFailureHandler().onFailure(request, response, new AuthenticationException("Token is mismatch"));
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
        Assert.notNull(request, "Http servlet request must not be null");

        // Get from header
        String token = request.getHeader(API_TOKEN_HEADER_NAME);

        // Get from param
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(API_TOKEN_QUERY_NAME);

            log.debug("Got token from parameter: [{}: {}]", API_TOKEN_QUERY_NAME, token);
        } else {
            log.debug("Got token from header: [{}: {}]", API_TOKEN_HEADER_NAME, token);
        }

        return token;
    }
}
