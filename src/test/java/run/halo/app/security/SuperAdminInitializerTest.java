package run.halo.app.security;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureTestDatabase
class SuperAdminInitializerTest {

    @Autowired
    @SpyBean
    ExtensionClient client;

    @Autowired
    WebTestClient webClient;

    @Test
    void checkSuperAdminInitialization() {
        verify(client, times(1)).fetch(eq(User.class), eq("admin"));
        verify(client, times(1)).create(argThat(extension -> {
            if (extension instanceof User user) {
                return "admin".equals(user.getMetadata().getName());
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
                return "admin-super-role-binding".equals(roleBinding.getMetadata().getName());
            }
            return false;
        }));
    }

}
