package run.halo.app.identity.authorization;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * An authorization filter that restricts access to the URL.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {
    private AuthorizationRuleResolver ruleResolver;
    private AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();

    public AuthorizationFilter(RoleGetter roleGetter, RoleBindingLister roleBindingLister) {
        this.ruleResolver = new DefaultRuleResolver(roleGetter, roleBindingLister);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
        Authentication authentication = getAuthentication();
        UserDetails userDetails = createUserDetails(authentication);

        AttributesRecord attributes = new AttributesRecord(userDetails, requestInfo);

        // visitor rules
        AuthorizingVisitor authorizingVisitor = new AuthorizingVisitor(attributes);
        ruleResolver.visitRulesFor(userDetails, authorizingVisitor);

        if (!authorizingVisitor.isAllowed()) {
            // print errors
            showErrorMessage(authorizingVisitor.getErrors());
            // handle it
            accessDeniedHandler.handle(request, response,
                new AccessDeniedException("Access is denied"));
            return;
        }
        log.debug(authorizingVisitor.getReason());
        filterChain.doFilter(request, response);
    }

    private void showErrorMessage(List<Throwable> errors) {
        if (CollectionUtils.isEmpty(errors)) {
            return;
        }
        for (Throwable error : errors) {
            log.error("Access decision error: ", error);
        }
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException(
                "An Authentication object was not found in the SecurityContext");
        }
        return authentication;
    }

    private UserDetails createUserDetails(Authentication authentication) {
        Assert.notNull(authentication, "The authentication must not be null.");
        return User.withUsername(authentication.getName())
            .authorities(authentication.getAuthorities())
            .password("N/A")
            .build();
    }

    public void setRuleResolver(AuthorizationRuleResolver ruleResolver) {
        Assert.notNull(ruleResolver, "ruleResolver must not be null.");
        this.ruleResolver = ruleResolver;
    }

    public void setAccessDeniedHandler(AccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }
}
