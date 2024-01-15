package run.halo.app.security.authorization;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.service.RoleService;

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
        ServerHttpRequest request = context.getExchange().getRequest();
        RequestInfo requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);

        return authentication.flatMap(auth -> this.ruleResolver.visitRules(auth, requestInfo)
                .doOnNext(visitor -> showErrorMessage(visitor.getErrors()))
                .filter(AuthorizingVisitor::isAllowed)
                .map(visitor -> new AuthorizationDecision(isGranted(auth)))
                .switchIfEmpty(Mono.fromSupplier(() -> new AuthorizationDecision(false))));
    }

    private boolean isGranted(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private void showErrorMessage(List<Throwable> errors) {
        if (errors != null) {
            errors.forEach(error -> log.error("Access decision error", error));
        }
    }

}
