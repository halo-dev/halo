package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.DuplicateNameException;

/** Tests for {@link DefaultSuperAdminInitializer}. */
class DefaultSuperAdminInitializerTest {

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    void createAdminShouldSetSystemProtectionFinalizer() {
        var passwordEncoder = mock(PasswordEncoder.class);
        var initializer = new DefaultSuperAdminInitializer(null, passwordEncoder);

        var admin = initializer.createAdmin("admin", "password", "admin@example.com");

        assertThat(admin.getMetadata()).isNotNull();
        assertThat(admin.getMetadata().getFinalizers()).isNotNull();
        assertThat(admin.getMetadata().getFinalizers()).contains(MetadataUtil.SYSTEM_FINALIZER);
    }

    @Test
    void shouldCreateAdminAndSuperRoleBinding() {
        var client = mock(ReactiveExtensionClient.class);
        var initializer = new DefaultSuperAdminInitializer(client, passwordEncoder);
        when(client.fetch(User.class, "admin")).thenReturn(Mono.empty());
        when(client.create(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(client.fetch(RoleBinding.class, "admin-super-role-binding")).thenReturn(Mono.empty());
        when(client.create(any(RoleBinding.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(initializer.initialize(param("admin"))).verifyComplete();

        verify(client).create(any(User.class));
        verify(client).create(any(RoleBinding.class));
    }

    @Test
    void shouldEnsureSuperRoleBindingWhenAdminAlreadyExists() {
        var client = mock(ReactiveExtensionClient.class);
        var initializer = new DefaultSuperAdminInitializer(client, passwordEncoder);
        var existingAdmin = new User();
        existingAdmin.setMetadata(new Metadata());
        existingAdmin.getMetadata().setName("admin");
        existingAdmin.setSpec(new User.UserSpec());
        existingAdmin.getSpec().setPassword(passwordEncoder.encode("password"));
        when(client.fetch(User.class, "admin")).thenReturn(Mono.just(existingAdmin));
        when(client.fetch(RoleBinding.class, "admin-super-role-binding")).thenReturn(Mono.empty());
        when(client.create(any(RoleBinding.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(initializer.initialize(param("admin"))).verifyComplete();

        // an existing admin must not be created (nor its password be reset) again
        verify(client, never()).create(any(User.class));
        // but the missing super role binding must be recovered
        verify(client).create(any(RoleBinding.class));
    }

    @Test
    void shouldNotFailWhenSuperRoleBindingAlreadyExists() {
        var client = mock(ReactiveExtensionClient.class);
        var initializer = new DefaultSuperAdminInitializer(client, passwordEncoder);
        var existingAdmin = new User();
        existingAdmin.setMetadata(new Metadata());
        existingAdmin.getMetadata().setName("admin");
        existingAdmin.setSpec(new User.UserSpec());
        existingAdmin.getSpec().setPassword(passwordEncoder.encode("password"));
        when(client.fetch(User.class, "admin")).thenReturn(Mono.just(existingAdmin));
        when(client.fetch(RoleBinding.class, "admin-super-role-binding")).thenReturn(Mono.just(new RoleBinding()));

        StepVerifier.create(initializer.initialize(param("admin"))).verifyComplete();

        verify(client, never()).create(any(User.class));
        verify(client, never()).create(any(RoleBinding.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRequireOriginalPasswordBeforeBindingExistingAdmin(boolean concurrentCreation) {
        var client = mock(ReactiveExtensionClient.class);
        var initializer = new DefaultSuperAdminInitializer(client, passwordEncoder);
        var existingAdmin = initializer.createAdmin("admin", "original-password", "admin@example.com");
        if (concurrentCreation) {
            when(client.fetch(User.class, "admin")).thenReturn(Mono.empty());
            when(client.create(any(User.class))).thenReturn(Mono.error(new DuplicateNameException()));
            when(client.get(User.class, "admin")).thenReturn(Mono.just(existingAdmin));
        } else {
            when(client.fetch(User.class, "admin")).thenReturn(Mono.just(existingAdmin));
        }

        StepVerifier.create(initializer.initialize(param("admin"))).verifyError(BadCredentialsException.class);

        verify(client, never()).fetch(RoleBinding.class, "admin-super-role-binding");
        verify(client, never()).create(any(RoleBinding.class));
        verify(client, never()).update(any(User.class));
        assertThat(passwordEncoder.matches(
                        "original-password", existingAdmin.getSpec().getPassword()))
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"hidden", "disabled", "deleting", "no-password"})
    void shouldRejectUnavailableExistingAdmin(String state) {
        var client = mock(ReactiveExtensionClient.class);
        var initializer = new DefaultSuperAdminInitializer(client, passwordEncoder);
        var existingAdmin = initializer.createAdmin("admin", "password", "admin@example.com");
        switch (state) {
            case "hidden" -> existingAdmin.getMetadata().setLabels(Map.of(User.HIDDEN_USER_LABEL, "true"));
            case "disabled" -> existingAdmin.getSpec().setDisabled(true);
            case "deleting" -> existingAdmin.getMetadata().setDeletionTimestamp(java.time.Instant.now());
            case "no-password" -> existingAdmin.getSpec().setPassword(null);
            default -> throw new IllegalArgumentException(state);
        }
        when(client.fetch(User.class, "admin")).thenReturn(Mono.just(existingAdmin));

        StepVerifier.create(initializer.initialize(param("admin"))).verifyError(BadCredentialsException.class);

        verify(client, never()).create(any(RoleBinding.class));
        verify(client, never()).update(any(User.class));
    }

    private static SuperAdminInitializer.InitializationParam param(String username) {
        return SuperAdminInitializer.InitializationParam.builder()
                .username(username)
                .password("password")
                .email("admin@example.com")
                .build();
    }
}
