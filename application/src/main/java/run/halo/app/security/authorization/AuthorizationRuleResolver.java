package run.halo.app.security.authorization;

import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface AuthorizationRuleResolver {

    Mono<AuthorizingVisitor> visitRules(UserDetails user, RequestInfo requestInfo);
}
