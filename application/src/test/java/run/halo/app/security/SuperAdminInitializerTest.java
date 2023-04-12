package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ReactiveExtensionClient;

@Disabled
@SpringBootTest(properties = {"halo.security.initializer.disabled=false",
    "halo.security.initializer.super-admin-username=fake-admin",
    "halo.security.initializer.super-admin-password=fake-password",
    "halo.required-extension-disabled=true",
    "halo.theme.initializer.disabled=true"})
@AutoConfigureWebTestClient
@AutoConfigureTestDatabase
class SuperAdminInitializerTest {

    @SpyBean
    ReactiveExtensionClient client;

    @Autowired
    WebTestClient webClient;

    @Autowired
    PasswordEncoder encoder;

    @Test
    void checkSuperAdminInitialization() {
        verify(client, times(1)).create(argThat(extension -> {
            if (extension instanceof User user) {
                return "fake-admin".equals(user.getMetadata().getName())
                    && encoder.matches("fake-password", user.getSpec().getPassword());
            }
            return false;
        }));
        verify(client, times(1)).create(argThat(extension -> {
            if (extension instanceof Role role) {
                return "super-role".equals(role.getMetadata().getName());
            }
            return false;
        }));
        verify(client, times(1)).create(argThat(extension -> {
            if (extension instanceof RoleBinding roleBinding) {
                return "fake-admin-super-role-binding".equals(roleBinding.getMetadata().getName());
            }
            return false;
        }));
    }

    @Test
    void checkUsername() {
        assertThatThrownBy(() -> SuperAdminInitializer.validateUsername(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Super administrator username must not be blank");

        assertThatThrownBy(() -> SuperAdminInitializer.validateUsername("abc-"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SuperAdminInitializer.validateUsername("ab?c-"))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> SuperAdminInitializer.validateUsername("asdD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("""
                Super administrator username must be a valid subdomain name, the name must:
                1. contain no more than 253 characters
                2. contain only lowercase alphanumeric characters, '-' or '.'
                3. start with an alphanumeric character
                4. end with an alphanumeric character
                """);

        Assertions.assertDoesNotThrow(() -> SuperAdminInitializer.validateUsername("1st"));
        Assertions.assertDoesNotThrow(() -> SuperAdminInitializer.validateUsername("ast"));
        Assertions.assertDoesNotThrow(() -> SuperAdminInitializer.validateUsername("ast1"));
        Assertions.assertDoesNotThrow(() -> SuperAdminInitializer.validateUsername("ast-1"));
    }

}
