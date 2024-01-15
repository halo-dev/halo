package run.halo.app.security.authorization;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.service.RoleService;

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
        var roleNames = listBoundRoleNames(authentication.getAuthorities());
        var record = new AttributesRecord(authentication, requestInfo);
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

    private static Set<String> listBoundRoleNames(
        Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .map(authority -> {
                authority = StringUtils.removeStart(authority, AuthorityUtils.SCOPE_PREFIX);
                authority = StringUtils.removeStart(authority, AuthorityUtils.ROLE_PREFIX);
                return authority;
            })
            .collect(Collectors.toSet());
    }
}
