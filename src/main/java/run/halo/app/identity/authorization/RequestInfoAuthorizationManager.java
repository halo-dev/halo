package run.halo.app.identity.authorization;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class RequestInfoAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    private AuthorizationRuleResolver ruleResolver;

    public RequestInfoAuthorizationManager(RoleGetter roleGetter) {
        this.ruleResolver = new DefaultRuleResolver(roleGetter);
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier,
        RequestAuthorizationContext requestContext) {
        HttpServletRequest request = requestContext.getRequest();
        RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);

        Authentication authentication = authenticationSupplier.get();
        UserDetails userDetails = createUserDetails(authentication);

        AttributesRecord attributes = new AttributesRecord(userDetails, requestInfo);

        // visitor rules
        AuthorizingVisitor authorizingVisitor = new AuthorizingVisitor(attributes);
        ruleResolver.visitRulesFor(userDetails, authorizingVisitor);

        if (!authorizingVisitor.isAllowed()) {
            // print errors
            showErrorMessage(authorizingVisitor.getErrors());
            return new AuthorizationDecision(false);
        }

        return new AuthorizationDecision(isGranted(authentication));
    }

    private void showErrorMessage(List<Throwable> errors) {
        if (CollectionUtils.isEmpty(errors)) {
            return;
        }
        for (Throwable error : errors) {
            log.error("Access decision error: ", error);
        }
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

    private boolean isGranted(Authentication authentication) {
        return authentication != null && isNotAnonymous(authentication)
            && authentication.isAuthenticated();
    }

    private boolean isNotAnonymous(Authentication authentication) {
        return !this.trustResolver.isAnonymous(authentication);
    }

}
