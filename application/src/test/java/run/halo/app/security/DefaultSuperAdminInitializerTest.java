package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import run.halo.app.extension.MetadataUtil;

/**
 * Tests for {@link DefaultSuperAdminInitializer}.
 */
class DefaultSuperAdminInitializerTest {

    @Test
    void createAdminShouldSetSystemProtectionFinalizer() {
        var passwordEncoder = mock(PasswordEncoder.class);
        var initializer = new DefaultSuperAdminInitializer(null, passwordEncoder);

        var admin = initializer.createAdmin("admin", "password", "admin@example.com");

        assertThat(admin.getMetadata()).isNotNull();
        assertThat(admin.getMetadata().getFinalizers()).isNotNull();
        assertThat(admin.getMetadata().getFinalizers())
            .contains(MetadataUtil.SYSTEM_FINALIZER);
    }
}
