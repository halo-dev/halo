package run.halo.app.security.authorization;

import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface AuthorizationRuleResolver {

    Mono<AuthorizingVisitor> visitRules(Authentication authentication, RequestInfo requestInfo);
}
