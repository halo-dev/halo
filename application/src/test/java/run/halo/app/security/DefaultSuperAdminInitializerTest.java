package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;

/** Tests for {@link DefaultSuperAdminInitializer}. */
class DefaultSuperAdminInitializerTest {

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
        var initializer = new DefaultSuperAdminInitializer(client, mock(PasswordEncoder.class));
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
        var initializer = new DefaultSuperAdminInitializer(client, mock(PasswordEncoder.class));
        var existingAdmin = new User();
        existingAdmin.setMetadata(new Metadata());
        existingAdmin.getMetadata().setName("admin");
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
        var initializer = new DefaultSuperAdminInitializer(client, mock(PasswordEncoder.class));
        var existingAdmin = new User();
        existingAdmin.setMetadata(new Metadata());
        existingAdmin.getMetadata().setName("admin");
        when(client.fetch(User.class, "admin")).thenReturn(Mono.just(existingAdmin));
        when(client.fetch(RoleBinding.class, "admin-super-role-binding")).thenReturn(Mono.just(new RoleBinding()));

        StepVerifier.create(initializer.initialize(param("admin"))).verifyComplete();

        verify(client, never()).create(any(User.class));
        verify(client, never()).create(any(RoleBinding.class));
    }

    private static SuperAdminInitializer.InitializationParam param(String username) {
        return SuperAdminInitializer.InitializationParam.builder()
                .username(username)
                .password("password")
                .email("admin@example.com")
                .build();
    }
}
