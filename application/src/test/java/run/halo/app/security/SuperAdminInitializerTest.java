package run.halo.app.security;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
}
