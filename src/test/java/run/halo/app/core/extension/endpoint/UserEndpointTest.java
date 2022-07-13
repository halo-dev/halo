package run.halo.app.core.extension.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
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
        // prevent from initializing the super admin.
        when(client.fetch(User.class, "admin")).thenReturn(Optional.of(mock(User.class)));
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

    @Nested
    class GrantPermissionEndpointTest {

        @Test
        @WithMockUser("fake-user")
        void shouldGetBadRequestIfRequestBodyIsEmpty() {
            webClient.post().uri("/apis/api.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

            // Why one more time to verify? Because the SuperAdminInitializer will fetch admin user.
            verify(client, never()).fetch(same(User.class), eq("fake-user"));
            verify(client, never()).fetch(same(Role.class), eq("fake-role"));
        }

        @Test
        @WithMockUser("fake-user")
        void shouldGetNotFoundIfUserNotFound() {
            when(client.fetch(User.class, "fake-user")).thenReturn(Optional.empty());
            when(client.fetch(Role.class, "fake-role")).thenReturn(Optional.of(mock(Role.class)));

            webClient.post().uri("/apis/api.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                .exchange()
                .expectStatus().isNotFound();

            verify(client, times(1)).fetch(same(User.class), eq("fake-user"));
            verify(client, never()).fetch(same(Role.class), eq("fake-role"));
        }

        @Test
        @WithMockUser("fake-user")
        void shouldGetNotFoundIfRoleNotFound() {
            when(client.fetch(User.class, "fake-user")).thenReturn(Optional.of(mock(User.class)));
            when(client.fetch(Role.class, "fake-role")).thenReturn(Optional.empty());

            webClient.post().uri("/apis/api.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                .exchange()
                .expectStatus().isNotFound();

            verify(client, times(1)).fetch(same(User.class), eq("fake-user"));
            verify(client, times(1)).fetch(same(Role.class), eq("fake-role"));
        }

        @Test
        @WithMockUser("fake-user")
        void shouldCreateRoleBindingIfNotExist() {
            when(client.fetch(User.class, "fake-user")).thenReturn(Optional.of(mock(User.class)));
            var role = mock(Role.class);
            when(client.fetch(Role.class, "fake-role")).thenReturn(Optional.of(role));

            webClient.post().uri("/apis/api.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                .exchange()
                .expectStatus().isOk();

            verify(client, times(1)).fetch(same(User.class), eq("fake-user"));
            verify(client, times(1)).fetch(same(Role.class), eq("fake-role"));
            verify(client, times(1)).create(RoleBinding.create("fake-user", "fake-role"));
            verify(client, never()).update(isA(RoleBinding.class));
            verify(client, never()).delete(isA(RoleBinding.class));
        }

        @Test
        @WithMockUser("fake-user")
        void shouldDeleteRoleBindingIfNotProvided() {
            when(client.fetch(User.class, "fake-user")).thenReturn(Optional.of(mock(User.class)));
            var role = mock(Role.class);
            when(client.fetch(Role.class, "fake-role")).thenReturn(Optional.of(role));
            var roleBinding = RoleBinding.create("fake-user", "non-provided-fake-role");
            when(client.list(same(RoleBinding.class), any(), any())).thenReturn(
                List.of(roleBinding));

            webClient.post().uri("/apis/api.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                .exchange()
                .expectStatus().isOk();

            verify(client, times(1)).fetch(same(User.class), eq("fake-user"));
            verify(client, times(1)).fetch(same(Role.class), eq("fake-role"));
            verify(client, times(1)).create(RoleBinding.create("fake-user", "fake-role"));
            verify(client, times(1)).delete(argThat(binding ->
                binding.getMetadata().getName().equals(roleBinding.getMetadata().getName())));
            verify(client, never()).update(isA(RoleBinding.class));
        }

    }

}