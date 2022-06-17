package run.halo.app.security;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.Role.PolicyRule;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;

@Slf4j
@Component
public class SuperAdminInitializer implements ApplicationListener<ApplicationStartedEvent> {

    private final ExtensionClient client;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminInitializer(ExtensionClient client, PasswordEncoder passwordEncoder) {
        this.client = client;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        client.fetch(User.class, "admin").ifPresentOrElse(user -> {
            // do nothing if admin has been initialized
        }, () -> {
            var admin = createAdmin();
            var superRole = createSuperRole();
            var roleBinding = bindAdminAndSuperRole(admin, superRole);
            client.create(admin);
            client.create(superRole);
            client.create(roleBinding);
        });
    }

    RoleBinding bindAdminAndSuperRole(User admin, Role superRole) {
        var metadata = new Metadata();
        metadata.setName("admin-super-role-binding");
        var roleRef = new RoleRef();
        roleRef.setName(superRole.getMetadata().getName());
        roleRef.setApiGroup(superRole.groupVersionKind().group());
        roleRef.setKind(superRole.getKind());

        var subject = new Subject();
        subject.setName(admin.getMetadata().getName());
        subject.setApiGroup(admin.groupVersionKind().group());
        subject.setKind(admin.groupVersionKind().kind());

        var roleBinding = new RoleBinding();
        roleBinding.setMetadata(metadata);
        roleBinding.setRoleRef(roleRef);
        roleBinding.setSubjects(List.of(subject));

        return roleBinding;
    }

    Role createSuperRole() {
        var metadata = new Metadata();
        metadata.setName("super-role");

        var superRule = new PolicyRule.Builder()
            .apiGroups("*")
            .resources("*")
            .nonResourceURLs("*")
            .verbs("*")
            .build();

        var role = new Role();
        role.setMetadata(metadata);
        role.setRules(List.of(superRule));
        return role;
    }

    User createAdmin() {
        var metadata = new Metadata();
        metadata.setName("admin");

        var spec = new UserSpec();
        spec.setDisplayName("Administrator");
        spec.setDisabled(false);
        spec.setRegisteredAt(Instant.now());
        spec.setTwoFactorAuthEnabled(false);
        spec.setEmail("admin@halo.run");
        // generate password
        var randomPassword = RandomStringUtils.randomAlphanumeric(16);
        log.info("=== Generated random password: {} for initial user: {} ===",
            randomPassword, metadata.getName());
        spec.setPassword(passwordEncoder.encode(randomPassword));

        var user = new User();
        user.setMetadata(metadata);
        user.setSpec(spec);
        return user;
    }
}
