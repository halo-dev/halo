package run.halo.app.security;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.properties.SecurityProperties.Initializer;

@Slf4j
public class SuperAdminInitializer {

    private static final String SUPER_ROLE_NAME = "super-role";

    private final ReactiveExtensionClient client;
    private final PasswordEncoder passwordEncoder;

    private final Initializer initializer;

    public SuperAdminInitializer(ReactiveExtensionClient client, PasswordEncoder passwordEncoder,
        Initializer initializer) {
        this.client = client;
        this.passwordEncoder = passwordEncoder;
        this.initializer = initializer;
    }

    @EventListener
    public Mono<Void> initialize(ApplicationReadyEvent readyEvent) {
        return client.fetch(User.class, initializer.getSuperAdminUsername())
            .switchIfEmpty(Mono.defer(() -> client.create(createAdmin())).flatMap(admin -> {
                var binding = bindAdminAndSuperRole(admin);
                return client.create(binding).thenReturn(admin);
            })).then();
    }

    RoleBinding bindAdminAndSuperRole(User admin) {
        var metadata = new Metadata();
        String name =
            String.join("-", initializer.getSuperAdminUsername(), SUPER_ROLE_NAME, "binding");
        metadata.setName(name);
        var roleRef = new RoleRef();
        roleRef.setName(SUPER_ROLE_NAME);
        roleRef.setApiGroup(Role.GROUP);
        roleRef.setKind(Role.KIND);

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

    User createAdmin() {
        String username = initializer.getSuperAdminUsername();
        validateUsername(username);

        var metadata = new Metadata();
        metadata.setName(username);

        var spec = new UserSpec();
        spec.setDisplayName("Administrator");
        spec.setDisabled(false);
        spec.setRegisteredAt(Instant.now());
        spec.setTwoFactorAuthEnabled(false);
        spec.setEmail("admin@halo.run");
        spec.setPassword(passwordEncoder.encode(getPassword()));

        var user = new User();
        user.setMetadata(metadata);
        user.setSpec(spec);
        return user;
    }

    static void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Super administrator username must not be blank");
        }
        boolean match =
            Pattern.matches("^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$",
                username);
        if (!match || username.length() > 63) {
            throw new IllegalArgumentException(
                """
                    Super administrator username must be a valid subdomain name, the name must:
                    1. contain no more than 253 characters
                    2. contain only lowercase alphanumeric characters, '-' or '.'
                    3. start with an alphanumeric character
                    4. end with an alphanumeric character
                    """);
        }
    }

    private String getPassword() {
        var password = this.initializer.getSuperAdminPassword();
        if (!StringUtils.hasText(password)) {
            // generate password
            password = RandomStringUtils.randomAlphanumeric(16);
            log.info("=== Generated random password: {} for super administrator: {} ===",
                password, this.initializer.getSuperAdminUsername());
        }
        return password;
    }
}
