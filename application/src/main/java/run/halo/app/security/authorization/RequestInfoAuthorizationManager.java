package run.halo.app.security.authorization;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.RoleService;

@Slf4j
public class RequestInfoAuthorizationManager
    implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final AuthorizationRuleResolver ruleResolver;

    public RequestInfoAuthorizationManager(RoleService roleService) {
        this.ruleResolver = new DefaultRuleResolver(roleService);
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,
        AuthorizationContext context) {
        var request = context.getExchange().getRequest();
        var requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);

        // We allow anonymous user to access some resources
        // so we don't invoke AuthenticationTrustResolver.isAuthenticated
        // to check if the user is authenticated
        return authentication.filter(Authentication::isAuthenticated)
            .flatMap(auth -> ruleResolver.visitRules(auth, requestInfo))
            .doOnNext(visitor -> showErrorMessage(visitor.getErrors()))
            .map(AuthorizingVisitor::isAllowed)
            .defaultIfEmpty(false)
            .map(AuthorizationDecision::new);
    }

    private void showErrorMessage(List<Throwable> errors) {
        if (errors != null) {
            errors.forEach(error -> log.error("Access decision error", error));
        }
    }

}
