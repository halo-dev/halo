package run.halo.app.core.extension.endpoint;

import static org.mockito.ArgumentMatchers.any;
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
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.infra.utils.JsonUtils;

@SpringBootTest
@AutoConfigureWebTestClient
@WithMockUser(username = "fake-user", password = "fake-password", roles = "fake-super-role")
class UserEndpointTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    RoleService roleService;

    @MockBean
    ReactiveExtensionClient client;

    @MockBean
    UserService userService;

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
        when(roleService.getMonoRole("authenticated")).thenReturn(Mono.just(role));
    }

    @Nested
    @DisplayName("GetUserDetail")
    class GetUserDetailTest {

        @Test
        void shouldResponseErrorIfUserNotFound() {
            when(client.get(User.class, "fake-user"))
                .thenReturn(Mono.error(new ExtensionNotFoundException()));
            webClient.get().uri("/apis/api.console.halo.run/v1alpha1/users/-")
                .exchange()
                .expectStatus().is5xxServerError();

            verify(client).get(User.class, "fake-user");
        }

        @Test
        void shouldGetCurrentUserDetail() {
            var metadata = new Metadata();
            metadata.setName("fake-user");
            var user = new User();
            user.setMetadata(metadata);
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(user));
            webClient.get().uri("/apis/api.console.halo.run/v1alpha1/users/-")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("ChangePassword")
    class ChangePasswordTest {

        @Test
        void shouldUpdateMyPasswordCorrectly() {
            var user = new User();
            when(userService.updateWithRawPassword("fake-user", "new-password"))
                .thenReturn(Mono.just(user));
            webClient.put().uri("/apis/api.console.halo.run/v1alpha1/users/-/password")
                .bodyValue(new UserEndpoint.ChangePasswordRequest("new-password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(user);

            verify(userService, times(1)).updateWithRawPassword("fake-user", "new-password");
        }

        @Test
        void shouldUpdateOtherPasswordCorrectly() {
            var user = new User();
            when(userService.updateWithRawPassword("another-fake-user", "new-password"))
                .thenReturn(Mono.just(user));
            webClient.put()
                .uri("/apis/api.console.halo.run/v1alpha1/users/another-fake-user/password")
                .bodyValue(new UserEndpoint.ChangePasswordRequest("new-password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(user);

            verify(userService, times(1)).updateWithRawPassword("another-fake-user",
                "new-password");
        }

    }

    @Nested
    @DisplayName("GrantPermission")
    class GrantPermissionEndpointTest {

        @BeforeEach
        void setUp() {
            when(client.list(same(RoleBinding.class), any(), any())).thenReturn(Flux.empty());
            when(client.get(User.class, "fake-user"))
                .thenReturn(Mono.error(new ExtensionNotFoundException()));
        }

        @Test
        void shouldGetBadRequestIfRequestBodyIsEmpty() {
            webClient.post().uri("/apis/api.console.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

            // Why one more time to verify? Because the SuperAdminInitializer will fetch admin user.
            verify(client, never()).fetch(same(User.class), eq("fake-user"));
            verify(client, never()).fetch(same(Role.class), eq("fake-role"));
        }

        @Test
        void shouldGetNotFoundIfUserNotFound() {
            when(client.get(User.class, "fake-user"))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
            when(client.get(Role.class, "fake-role")).thenReturn(Mono.just(mock(Role.class)));

            webClient.post().uri("/apis/api.console.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                .exchange()
                .expectStatus().isNotFound();

            verify(client, times(1)).get(same(User.class), eq("fake-user"));
            verify(client, never()).get(same(Role.class), eq("fake-role"));
        }

        @Test
        void shouldGetNotFoundIfRoleNotFound() {
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(mock(User.class)));
            when(client.get(Role.class, "fake-role"))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

            webClient.post().uri("/apis/api.console.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                .exchange()
                .expectStatus().isNotFound();

            verify(client).get(User.class, "fake-user");
            verify(client).get(Role.class, "fake-role");
        }

        @Test
        void shouldCreateRoleBindingIfNotExist() {
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(mock(User.class)));
            var role = mock(Role.class);
            when(client.get(Role.class, "fake-role")).thenReturn(Mono.just(role));

            webClient.post().uri("/apis/api.console.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                .exchange()
                .expectStatus().isOk();

            verify(client).get(User.class, "fake-user");
            verify(client).get(Role.class, "fake-role");
            verify(client).create(RoleBinding.create("fake-user", "fake-role"));
            verify(client, never()).update(isA(RoleBinding.class));
            verify(client, never()).delete(isA(RoleBinding.class));
        }

        @Test
        void shouldDeleteRoleBindingIfNotProvided() {
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(mock(User.class)));
            var role = mock(Role.class);
            when(client.get(Role.class, "fake-role")).thenReturn(Mono.just(role));
            var roleBinding = RoleBinding.create("fake-user", "non-provided-fake-role");
            when(client.list(same(RoleBinding.class), any(), any()))
                .thenReturn(Flux.fromIterable(List.of(roleBinding)));

            webClient.post().uri("/apis/api.console.halo.run/v1alpha1/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role"))).exchange()
                .expectStatus().isOk();

            verify(client).get(User.class, "fake-user");
            verify(client).get(Role.class, "fake-role");
            verify(client).create(RoleBinding.create("fake-user", "fake-role"));
            verify(client)
                .delete(argThat(binding -> binding.getMetadata().getName()
                    .equals(roleBinding.getMetadata().getName())));
            verify(client, never()).update(isA(RoleBinding.class));
        }

        @Test
        void shouldGetPermission() {
            Role roleA = JsonUtils.jsonToObject("""
                {
                    "apiVersion": "v1alpha1",
                    "kind": "Role",
                    "metadata": {
                        "name": "test-A",
                        "annotations": {
                            "rbac.authorization.halo.run/ui-permissions": "[\\"permission-A\\"]",
                            "rbac.authorization.halo.run/ui-permissions-aggregated":
                             "[\\"permission-B\\"]"
                        }
                    },
                    "rules": []
                }
                """, Role.class);
            when(userService.listRoles(eq("fake-user"))).thenReturn(
                Flux.fromIterable(List.of(roleA)));

            webClient.get().uri("/apis/api.console.halo.run/v1alpha1/users/fake-user/permissions")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {  "roles": [{
                               "rules": [],
                               "apiVersion": "v1alpha1",
                               "kind": "Role",
                               "metadata": {
                                   "name": "test-A",
                                   "annotations": {
                                       "rbac.authorization.halo.run/ui-permissions":
                                        "[\\"permission-A\\"]",
                                       "rbac.authorization.halo.run/ui-permissions-aggregated":
                                        "[\\"permission-B\\"]"
                                   }
                               }
                           }],
                            "uiPermissions": [
                               "permission-A",
                               "permission-B"
                            ]
                        }
                    """);

            verify(userService, times(1)).listRoles(eq("fake-user"));
        }
    }

}