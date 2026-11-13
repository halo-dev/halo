package run.halo.app.security;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.DuplicateNameException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultSuperAdminInitializer implements SuperAdminInitializer {

    private final ReactiveExtensionClient client;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<Void> initialize(InitializationParam param) {
        return client.fetch(User.class, param.getUsername())
                .flatMap(admin -> verifyExistingAdmin(admin, param))
                .switchIfEmpty(Mono.defer(() -> client.create(
                                createAdmin(param.getUsername(), param.getPassword(), param.getEmail()))
                        // The user may be created concurrently by another setup request.
                        .onErrorResume(
                                DuplicateNameException.class,
                                e -> client.get(User.class, param.getUsername())
                                        .flatMap(admin -> verifyExistingAdmin(admin, param)))))
                // The user might exist without the role binding if a previous setup was interrupted,
                // so the binding must be ensured even if the user already exists.
                .flatMap(this::ensureSuperRoleBinding)
                .then();
    }

    private Mono<User> verifyExistingAdmin(User admin, InitializationParam param) {
        return Mono.fromCallable(() -> {
            var spec = admin.getSpec();
            if (Boolean.parseBoolean(MetadataUtil.nullSafeLabels(admin).get(User.HIDDEN_USER_LABEL))
                    || admin.getMetadata().getDeletionTimestamp() != null
                    || Boolean.TRUE.equals(spec.getDisabled())
                    || !StringUtils.hasText(spec.getPassword())
                    || !passwordEncoder.matches(param.getPassword(), spec.getPassword())) {
                throw new BadCredentialsException("The username or password is incorrect.");
            }
            return admin;
        });
    }

    Mono<User> ensureSuperRoleBinding(User admin) {
        var binding = bindAdminAndSuperRole(admin);
        return client.fetch(RoleBinding.class, binding.getMetadata().getName())
                .switchIfEmpty(Mono.defer(
                        () -> client.create(binding).onErrorResume(DuplicateNameException.class, e -> Mono.empty())))
                .thenReturn(admin);
    }

    RoleBinding bindAdminAndSuperRole(User admin) {
        String adminUserName = admin.getMetadata().getName();
        var metadata = new Metadata();
        String name = String.join("-", adminUserName, SUPER_ROLE_NAME, "binding");
        metadata.setName(name);
        var roleRef = new RoleRef();
        roleRef.setName(SUPER_ROLE_NAME);
        roleRef.setApiGroup(Role.GROUP);
        roleRef.setKind(Role.KIND);

        var subject = new Subject();
        subject.setName(adminUserName);
        subject.setApiGroup(admin.groupVersionKind().group());
        subject.setKind(admin.groupVersionKind().kind());

        var roleBinding = new RoleBinding();
        roleBinding.setMetadata(metadata);
        roleBinding.setRoleRef(roleRef);
        roleBinding.setSubjects(List.of(subject));

        return roleBinding;
    }

    User createAdmin(String username, String password, String email) {
        var metadata = new Metadata();
        metadata.setName(username);
        metadata.setFinalizers(Set.of(MetadataUtil.SYSTEM_FINALIZER));

        var spec = new UserSpec();
        spec.setDisplayName("Administrator");
        spec.setDisabled(false);
        spec.setRegisteredAt(Instant.now());
        spec.setTwoFactorAuthEnabled(false);
        spec.setEmail(email);
        spec.setPassword(passwordEncoder.encode(password));

        var user = new User();
        user.setMetadata(metadata);
        user.setSpec(spec);
        return user;
    }
}
