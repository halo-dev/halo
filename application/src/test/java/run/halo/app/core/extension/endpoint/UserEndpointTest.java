package run.halo.app.core.extension.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction;
import static run.halo.app.extension.GroupVersionKind.fromExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.infra.utils.JsonUtils;

@SpringBootTest
@AutoConfigureWebTestClient
@WithMockUser(username = "fake-user", password = "fake-password", roles = "fake-super-role")
class UserEndpointTest {

    WebTestClient webClient;

    @Mock
    RoleService roleService;

    @Mock
    ReactiveExtensionClient client;

    @Mock
    UserService userService;

    @InjectMocks
    UserEndpoint endpoint;

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
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .build();
        webClient = webClient.mutateWith(csrf());
    }

    @Nested
    class UserListTest {

        @Test
        void shouldListEmptyUsersWhenNoUsers() {
            when(client.list(same(User.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(ListResult.emptyResult()));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(0)
                .jsonPath("$.total").isEqualTo(0);
        }

        @Test
        void shouldListUsersWhenUserPresent() {
            var users = List.of(
                createUser("fake-user-1"),
                createUser("fake-user-2"),
                createUser("fake-user-3")
            );
            var expectResult = new ListResult<>(users);
            when(roleService.list(anySet())).thenReturn(Flux.empty());
            when(client.list(same(User.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(3)
                .jsonPath("$.total").isEqualTo(3);
        }

        @Test
        void shouldFilterUsersWhenKeywordProvided() {
            var expectUser =
                createUser("fake-user-2", "expected display name");
            var unexpectedUser1 =
                createUser("fake-user-1", "first fake display name");
            var unexpectedUser2 =
                createUser("fake-user-3", "second fake display name");
            var users = List.of(
                expectUser
            );
            var expectResult = new ListResult<>(users);
            when(client.list(same(User.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(expectResult));
            when(roleService.list(anySet())).thenReturn(Flux.empty());

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/users?keyword=Expected")
                .exchange()
                .expectStatus().isOk();

            verify(client).list(same(User.class), argThat(
                    predicate -> predicate.test(expectUser)
                        && !predicate.test(unexpectedUser1)
                        && !predicate.test(unexpectedUser2)),
                any(), anyInt(), anyInt());
        }

        @Test
        void shouldFilterUsersWhenRoleProvided() {
            var expectUser =
                JsonUtils.jsonToObject("""
                    {
                        "apiVersion": "v1alpha1",
                        "kind": "User",
                        "metadata": {
                            "name": "alice",
                            "annotations": {
                                "rbac.authorization.halo.run/role-names": "[\\"guest\\"]"
                            }
                        }
                    }
                    """, User.class);
            var unexpectedUser1 =
                JsonUtils.jsonToObject("""
                    {
                        "apiVersion": "v1alpha1",
                        "kind": "User",
                        "metadata": {
                            "name": "admin",
                            "annotations": {
                                "rbac.authorization.halo.run/role-names": "[\\"super-role\\"]"
                            }
                        }
                    }
                    """, User.class);
            var unexpectedUser2 =
                JsonUtils.jsonToObject("""
                    {
                        "apiVersion": "v1alpha1",
                        "kind": "User",
                        "metadata": {
                            "name": "joey",
                            "annotations": {}
                        }
                    }
                    """, User.class);
            var users = List.of(
                expectUser
            );
            var expectResult = new ListResult<>(users);
            when(client.list(same(User.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(expectResult));
            when(roleService.list(anySet())).thenReturn(Flux.empty());

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/users?role=guest")
                .exchange()
                .expectStatus().isOk();

            verify(client).list(same(User.class), argThat(
                    predicate -> predicate.test(expectUser)
                        && !predicate.test(unexpectedUser1)
                        && !predicate.test(unexpectedUser2)),
                any(), anyInt(), anyInt());
        }

        @Test
        void shouldSortUsersWhenCreationTimestampSet() {
            var expectUser =
                createUser("fake-user-2", "expected display name");
            var unexpectedUser1 =
                createUser("fake-user-1", "first fake display name");
            var unexpectedUser2 =
                createUser("fake-user-3", "second fake display name");
            var expectResult = new ListResult<>(List.of(expectUser));
            when(client.list(same(User.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(expectResult));
            when(roleService.list(anySet())).thenReturn(Flux.empty());

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/users?sort=creationTimestamp,desc")
                .exchange()
                .expectStatus().isOk();

            verify(client).list(same(User.class), any(), argThat(comparator -> {
                var now = Instant.now();
                var users = new ArrayList<>(List.of(
                    createUser("fake-user-a", now),
                    createUser("fake-user-b", now.plusSeconds(1)),
                    createUser("fake-user-c", now.plusSeconds(2))
                ));
                users.sort(comparator);
                return Objects.deepEquals(users, List.of(
                    createUser("fake-user-c", now.plusSeconds(2)),
                    createUser("fake-user-b", now.plusSeconds(1)),
                    createUser("fake-user-a", now)
                ));
            }), anyInt(), anyInt());
        }

        User createUser(String name) {
            return createUser(name, "fake display name");
        }

        User createUser(String name, String displayName) {
            var metadata = new Metadata();
            metadata.setName(name);
            metadata.setCreationTimestamp(Instant.now());
            var spec = new User.UserSpec();
            spec.setDisplayName(displayName);
            var user = new User();
            user.setMetadata(metadata);
            user.setSpec(spec);
            return user;
        }

        User createUser(String name, Instant creationTimestamp) {
            var metadata = new Metadata();
            metadata.setName(name);
            metadata.setCreationTimestamp(creationTimestamp);
            var spec = new User.UserSpec();
            var user = new User();
            user.setMetadata(metadata);
            user.setSpec(spec);
            return user;
        }
    }

    @Nested
    @DisplayName("GetUserDetail")
    class GetUserDetailTest {

        @Test
        void shouldResponseErrorIfUserNotFound() {
            when(userService.getUser("fake-user"))
                .thenReturn(Mono.error(
                    new ExtensionNotFoundException(fromExtension(User.class), "fake-user")));
            webClient.get().uri("/users/-")
                .exchange()
                .expectStatus().isNotFound();

            verify(userService).getUser(eq("fake-user"));
        }

        @Test
        void shouldGetCurrentUserDetail() {
            var metadata = new Metadata();
            metadata.setName("fake-user");
            var user = new User();
            user.setMetadata(metadata);
            Map<String, String> annotations =
                Map.of(User.ROLE_NAMES_ANNO, JsonUtils.objectToJson(Set.of("role-A")));
            user.getMetadata().setAnnotations(annotations);
            when(userService.getUser("fake-user")).thenReturn(Mono.just(user));
            Role role = new Role();
            role.setMetadata(new Metadata());
            role.getMetadata().setName("role-A");
            role.setRules(List.of());
            when(roleService.list(anySet())).thenReturn(Flux.just(role));
            webClient.get().uri("/users/-")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserEndpoint.DetailedUser.class)
                .isEqualTo(new UserEndpoint.DetailedUser(user, List.of(role)));
            verify(roleService).list(eq(Set.of("role-A")));
        }
    }

    @Nested
    @DisplayName("UpdateProfile")
    class UpdateProfileTest {

        @Test
        void shouldUpdateProfileCorrectly() {
            var currentUser = createUser("fake-user");
            var updatedUser = createUser("fake-user");
            var requestUser = createUser("fake-user");

            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));
            when(client.update(currentUser)).thenReturn(Mono.just(updatedUser));

            webClient.put().uri("/users/-")
                .bodyValue(requestUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(updatedUser);

            verify(client).get(User.class, "fake-user");
            verify(client).update(currentUser);
        }

        @Test
        void shouldGetErrorIfUsernameMismatch() {
            var currentUser = createUser("fake-user");
            var updatedUser = createUser("fake-user");
            var requestUser = createUser("another-fake-user");

            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));
            when(client.update(currentUser)).thenReturn(Mono.just(updatedUser));

            webClient.put().uri("/users/-")
                .bodyValue(requestUser)
                .exchange()
                .expectStatus().isBadRequest();

            verify(client).get(User.class, "fake-user");
            verify(client, never()).update(currentUser);
        }

        User createUser(String name) {
            var spec = new User.UserSpec();
            spec.setEmail("hi@halo.run");
            spec.setBio("Fake bio");
            spec.setDisplayName("Faker");
            spec.setPassword("fake-password");

            var metadata = new Metadata();
            metadata.setName(name);

            var user = new User();
            user.setSpec(spec);
            user.setMetadata(metadata);
            return user;
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
            webClient.put().uri("/users/-/password")
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
                .uri("/users/another-fake-user/password")
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
                .thenReturn(Mono.error(
                    new ExtensionNotFoundException(fromExtension(User.class), "fake-user")));
        }

        @Test
        void shouldGetBadRequestIfRequestBodyIsEmpty() {
            webClient.post().uri("/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

            // Why one more time to verify? Because the SuperAdminInitializer will fetch admin user.
            verify(client, never()).fetch(same(User.class), eq("fake-user"));
            verify(client, never()).fetch(same(Role.class), eq("fake-role"));
        }

        @Test
        void shouldGrantPermission() {
            when(userService.grantRoles("fake-user", Set.of("fake-role"))).thenReturn(Mono.empty());

            webClient.post().uri("/users/fake-user/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                .exchange()
                .expectStatus().isOk();
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
                            "rbac.authorization.halo.run/ui-permissions": "[\\"permission-A\\"]"
                        }
                    },
                    "rules": []
                }
                """, Role.class);
            when(userService.listRoles(eq("fake-user"))).thenReturn(
                Flux.fromIterable(List.of(roleA)));
            when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.just(roleA));

            webClient.get().uri("/users/fake-user/permissions")
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
                                        "[\\"permission-A\\"]"
                                   }
                               }
                           }],
                            "uiPermissions": [
                               "permission-A"
                            ]
                        }
                    """);

            verify(userService, times(1)).listRoles(eq("fake-user"));
        }
    }

    @Test
    void createWhenNameDuplicate() {
        when(userService.createUser(any(User.class), anySet()))
            .thenReturn(Mono.just(new User()));
        when(userService.updateWithRawPassword(anyString(), anyString()))
            .thenReturn(Mono.just(new User()));
        var userRequest = new UserEndpoint.CreateUserRequest("fake-user",
            "fake-email",
            "",
            "",
            "",
            "",
            "",
            Map.of(),
            Set.of());
        webClient.post().uri("/users")
            .bodyValue(userRequest)
            .exchange()
            .expectStatus().isOk();
    }
}
