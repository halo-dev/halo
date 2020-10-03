package run.halo.app.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.model.entity.User;
import run.halo.app.security.authentication.AuthenticationImpl;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.context.SecurityContextImpl;
import run.halo.app.security.handler.DefaultAuthenticationFailureHandler;
import run.halo.app.security.service.OneTimeTokenService;
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

import static run.halo.app.model.support.HaloConst.ADMIN_TOKEN_HEADER_NAME;
import static run.halo.app.model.support.HaloConst.ADMIN_TOKEN_QUERY_NAME;

/**
 * Admin authentication filter.
 *
 * @author johnniang
 */
@Slf4j
@Component
@Order(1)
public class AdminAuthenticationFilter extends AbstractAuthenticationFilter {

    private final HaloProperties haloProperties;

    private final UserService userService;

    public AdminAuthenticationFilter(AbstractStringCacheStore cacheStore,
            UserService userService,
            HaloProperties haloProperties,
            OptionService optionService,
            OneTimeTokenService oneTimeTokenService,
            ObjectMapper objectMapper) {
        super(haloProperties, optionService, cacheStore, oneTimeTokenService);
        this.userService = userService;
        this.haloProperties = haloProperties;

        addUrlPatterns("/api/admin/**", "/api/content/comments");

        addExcludeUrlPatterns(
                "/api/admin/login",
                "/api/admin/refresh/*",
                "/api/admin/installations",
                "/api/admin/migrations/halo",
                "/api/admin/is_installed",
                "/api/admin/password/code",
                "/api/admin/password/reset",
                "/api/admin/login/precheck"
        );

        // set failure handler
        DefaultAuthenticationFailureHandler failureHandler = new DefaultAuthenticationFailureHandler();
        failureHandler.setProductionEnv(haloProperties.isProductionEnv());
        failureHandler.setObjectMapper(objectMapper);

        setFailureHandler(failureHandler);

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
            throw new AuthenticationException("未登录，请登录后访问");
        }

        // Get user id from cache
        Optional<Integer> optionalUserId = cacheStore.getAny(SecurityUtils.buildTokenAccessKey(token), Integer.class);

        if (!optionalUserId.isPresent()) {
            throw new AuthenticationException("Token 已过期或不存在").setErrorData(token);
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
        return getTokenFromRequest(request, ADMIN_TOKEN_QUERY_NAME, ADMIN_TOKEN_HEADER_NAME);
    }

}
