package run.halo.app.identity.authorization;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {
    private AuthorizationRuleResolver ruleResolver;

    public AuthorizationFilter(RoleGetter roleGetter, RoleBindingLister roleBindingLister) {
        this.ruleResolver = new DefaultRuleResolver(roleGetter, roleBindingLister);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = new User(authentication.getName(), "123", true,
            true,true,true,Collections.emptyList());
        AttributesRecord attributes = new AttributesRecord(userDetails, requestInfo);
        AuthorizingVisitor authorizingVisitor = new AuthorizingVisitor(attributes);
        ruleResolver.visitRulesFor(userDetails, authorizingVisitor);
        if (authorizingVisitor.isAllowed()) {
            filterChain.doFilter(request, response);
            return;
        }
        log.error("{}", authorizingVisitor.getErrors());
        response.getWriter().write("Unauthorized Access --->" + authorizingVisitor.getReason());
    }
}
