package run.halo.app.security.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.service.DefaultRoleBindingService;
import run.halo.app.core.extension.service.RoleBindingService;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@Slf4j
public class DefaultRuleResolver implements AuthorizationRuleResolver {
    private static final String AUTHENTICATED_ROLE = "authenticated";
    private RoleService roleService;

    private RoleBindingService roleBindingService = new DefaultRoleBindingService();

    public DefaultRuleResolver(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public PolicyRuleList rulesFor(UserDetails user) {
        PolicyRuleList policyRules = new PolicyRuleList();
        visitRulesFor(user, (source, rule, err) -> {
            if (rule != null) {
                policyRules.add(rule);
            }
            if (err != null) {
                policyRules.addError(err);
            }
            return true;
        });
        return policyRules;
    }

    @Override
    public void visitRulesFor(UserDetails user, RuleAccumulator visitor) {
        Set<String> roleNamesImmutable =
            roleBindingService.listBoundRoleNames(user.getAuthorities());
        Set<String> roleNames = new HashSet<>(roleNamesImmutable);
        if (!AnonymousUserConst.PRINCIPAL.equals(user.getUsername())) {
            roleNames.add(AUTHENTICATED_ROLE);
            roleNames.add(AnonymousUserConst.Role);
        }

        List<Role.PolicyRule> rules = Collections.emptyList();
        for (String roleName : roleNames) {
            try {
                Role role = roleService.getRole(roleName);
                // fetch rules from role
                rules = fetchRules(role);
            } catch (Exception e) {
                if (visitor.visit(null, null, e)) {
                    // if visitor returns true, we continue visiting
                    continue;
                }
            }

            String source = roleBindingDescriber(roleName, user.getUsername());
            for (Role.PolicyRule rule : rules) {
                if (!visitor.visit(source, rule, null)) {
                    return;
                }
            }
        }
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

    private List<Role.PolicyRule> fetchRules(Role role) {
        MetadataOperator metadata = role.getMetadata();
        if (metadata == null || metadata.getAnnotations() == null) {
            return role.getRules();
        }

        // merge policy rules
        String roleDependencyRules = metadata.getAnnotations()
            .get(Role.ROLE_DEPENDENCY_RULES);
        List<Role.PolicyRule> rules = convertFrom(roleDependencyRules);
        rules.addAll(role.getRules());
        return rules;
    }

    private List<Role.PolicyRule> convertFrom(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return JsonUtils.DEFAULT_JSON_MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    String roleBindingDescriber(String roleName, String subject) {
        return String.format("Binding role [%s] to [%s]", roleName, subject);
    }

    public void setRoleBindingService(RoleBindingService roleBindingService) {
        Assert.notNull(roleBindingService, "The roleBindingLister must not be null.");
        this.roleBindingService = roleBindingService;
    }
}
