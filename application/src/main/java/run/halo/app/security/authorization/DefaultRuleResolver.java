package run.halo.app.security.authorization;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.service.DefaultRoleBindingService;
import run.halo.app.core.extension.service.RoleBindingService;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.infra.AnonymousUserConst;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@Slf4j
public class DefaultRuleResolver implements AuthorizationRuleResolver {
    private static final String AUTHENTICATED_ROLE = "authenticated";
    private RoleService roleService;

    private RoleBindingService roleBindingService;

    public DefaultRuleResolver(RoleService roleService) {
        this.roleService = roleService;
        this.roleBindingService = new DefaultRoleBindingService();
    }

    @Override
    public Mono<AuthorizingVisitor> visitRules(UserDetails user, RequestInfo requestInfo) {
        var roleNamesImmutable = roleBindingService.listBoundRoleNames(user.getAuthorities());
        var roleNames = new HashSet<>(roleNamesImmutable);
        if (!AnonymousUserConst.PRINCIPAL.equals(user.getUsername())) {
            roleNames.add(AUTHENTICATED_ROLE);
            roleNames.add(AnonymousUserConst.Role);
        }

        var record = new AttributesRecord(user, requestInfo);
        var visitor = new AuthorizingVisitor(record);
        var stopVisiting = new AtomicBoolean(false);
        return roleService.listDependenciesFlux(roleNames)
            .filter(role -> !CollectionUtils.isEmpty(role.getRules()))
            .doOnNext(role -> {
                if (stopVisiting.get()) {
                    return;
                }
                String roleName = role.getMetadata().getName();
                var rules = role.getRules();
                var source = roleBindingDescriber(roleName, user.getUsername());
                for (var rule : rules) {
                    if (!visitor.visit(source, rule, null)) {
                        stopVisiting.set(true);
                        return;
                    }
                }
            })
            .takeUntil(item -> stopVisiting.get())
            .onErrorResume(t -> visitor.visit(null, null, t), t -> {
                log.warn("Error occurred when visiting rules", t);
                //Do nothing here
                return Mono.empty();
            })
            .then(Mono.just(visitor));
    }

    String roleBindingDescriber(String roleName, String subject) {
        return String.format("Binding role [%s] to [%s]", roleName, subject);
    }

    public void setRoleBindingService(RoleBindingService roleBindingService) {
        Assert.notNull(roleBindingService, "The roleBindingLister must not be null.");
        this.roleBindingService = roleBindingService;
    }
}
