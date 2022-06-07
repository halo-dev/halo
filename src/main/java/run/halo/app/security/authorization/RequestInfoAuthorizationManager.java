package run.halo.app.security.authorization;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

@Slf4j
public class RequestInfoAuthorizationManager
    implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    private final AuthorizationRuleResolver ruleResolver;

    public RequestInfoAuthorizationManager(RoleGetter roleGetter) {
        this.ruleResolver = new DefaultRuleResolver(roleGetter);
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,
        AuthorizationContext context) {
        ServerHttpRequest request = context.getExchange().getRequest();
        RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);

        return authentication.map(auth -> {
            UserDetails userDetails = this.createUserDetails(auth);
            var record = new AttributesRecord(userDetails, requestInfo);
            var visitor = new AuthorizingVisitor(record);
            ruleResolver.visitRulesFor(userDetails, visitor);
            if (!visitor.isAllowed()) {
                showErrorMessage(visitor.getErrors());
                return new AuthorizationDecision(false);
            }
            return new AuthorizationDecision(isGranted(auth));
        });
    }

    private boolean isGranted(Authentication authentication) {
        return authentication != null && isNotAnonymous(authentication)
            && authentication.isAuthenticated();
    }

    private boolean isNotAnonymous(Authentication authentication) {
        return !this.trustResolver.isAnonymous(authentication);
    }

    private UserDetails createUserDetails(Authentication authentication) {
        Assert.notNull(authentication, "The authentication must not be null.");
        return User.withUsername(authentication.getName())
            .authorities(authentication.getAuthorities())
            .password("")
            .build();
    }

    private void showErrorMessage(List<Throwable> errors) {
        if (CollectionUtils.isEmpty(errors)) {
            return;
        }
        for (Throwable error : errors) {
            log.error("Access decision error: ", error);
        }
    }

}
