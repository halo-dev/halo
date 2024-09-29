package run.halo.app.security.authorization;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.RoleService;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@Slf4j
public class DefaultRuleResolver implements AuthorizationRuleResolver {

    private RoleService roleService;

    public DefaultRuleResolver(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public Mono<AuthorizingVisitor> visitRules(Authentication authentication,
        RequestInfo requestInfo) {
        var roleNames = AuthorityUtils.authoritiesToRoles(authentication.getAuthorities());
        var record = new AttributesRecord(requestInfo);
        var visitor = new AuthorizingVisitor(record);

        // If the request is an userspace scoped request,
        // then we should check whether the user is the owner of the userspace.
        if (StringUtils.isNotBlank(requestInfo.getUserspace())) {
            if (!authentication.getName().equals(requestInfo.getUserspace())) {
                return Mono.fromSupplier(() -> {
                    visitor.visit(null, null, null);
                    return visitor;
                });
            }
        }

        var stopVisiting = new AtomicBoolean(false);
        return roleService.listDependenciesFlux(roleNames)
            .filter(role -> !CollectionUtils.isEmpty(role.getRules()))
            .doOnNext(role -> {
                if (stopVisiting.get()) {
                    return;
                }
                String roleName = role.getMetadata().getName();
                var rules = role.getRules();
                var source = roleBindingDescriber(roleName, authentication.getName());
                for (var rule : rules) {
                    if (!visitor.visit(source, rule, null)) {
                        stopVisiting.set(true);
                        return;
                    }
                }
            })
            .takeUntil(item -> stopVisiting.get())
            .onErrorResume(t -> visitor.visit(null, null, t), t -> {
                log.error("Error occurred when visiting rules", t);
                //Do nothing here
                return Mono.empty();
            })
            .then(Mono.just(visitor));
    }

    String roleBindingDescriber(String roleName, String subject) {
        return String.format("Binding role [%s] to [%s]", roleName, subject);
    }

}
