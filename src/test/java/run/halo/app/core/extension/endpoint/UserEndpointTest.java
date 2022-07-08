package run.halo.app.core.extension.endpoint;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;

@SpringBootTest
@AutoConfigureWebTestClient
class UserEndpointTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    RoleService roleService;

    @MockBean
    ExtensionClient client;

    @BeforeEach
    void setUp() {
        // disable authorization
        var rule = new Role.PolicyRule.Builder()
            .apiGroups("*")
            .resources("*")
            .verbs("*")
            .build();
        var role = new Role();
        role.setRules(List.of(rule));
        when(roleService.getRole(anyString())).thenReturn(role);
    }

    @Test
    @WithMockUser("fake-user")
    void shouldResponseErrorIfUserNotFound() {
        when(client.fetch(User.class, "fake-user")).thenReturn(Optional.empty());
        webClient.get().uri("/apis/api.halo.run/v1alpha1/users/-")
            .exchange()
            .expectStatus().is5xxServerError();
    }

    @Test
    @WithMockUser("fake-user")
    void shouldGetCurrentUserDetail() {
        var metadata = new Metadata();
        metadata.setName("fake-user");
        var user = new User();
        user.setMetadata(metadata);
        when(client.fetch(User.class, "fake-user")).thenReturn(Optional.of(user));
        webClient.get().uri("/apis/api.halo.run/v1alpha1/users/-")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(User.class)
            .isEqualTo(user);
    }
}