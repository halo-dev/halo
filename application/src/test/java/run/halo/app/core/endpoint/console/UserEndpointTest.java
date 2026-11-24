package run.halo.app.core.endpoint.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.security.web.server.authentication.SwitchUserWebFilter;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.UserNotFoundException;
import run.halo.app.infra.utils.JsonUtils;

@ExtendWith(MockitoExtension.class)
class UserEndpointTest {

    WebTestClient webClient;

    @Mock
    RoleService roleService;

    @Mock
    AttachmentService attachmentService;

    @Mock
    SystemConfigFetcher environmentFetcher;

    @Mock
    ReactiveExtensionClient client;

    @Mock
    UserService userService;

    @Mock
    Validator validator;

    @InjectMocks
    UserEndpoint endpoint;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .webFilter((exchange, chain) -> chain.filter(exchange)
                        .onErrorResume(ServerWebInputException.class, error -> {
                            var response = exchange.getResponse();
                            response.setStatusCode(error.getStatusCode());
                            response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
                            var messages = new ReloadableResourceBundleMessageSource();
                            messages.setBasename("file:src/main/resources/config/i18n/messages");
                            messages.setDefaultEncoding("UTF-8");
                            var body = JsonUtils.objectToJson(error.updateAndGetBody(messages, Locale.CHINESE))
                                    .getBytes(StandardCharsets.UTF_8);
                            return response.writeWith(
                                    Mono.just(response.bufferFactory().wrap(body)));
                        })
                        .onErrorResume(AccessDeniedException.class, error -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }))
                .apply(springSecurity())
                .build()
                .mutateWith(mockUser("fake-user").password("fake-password").roles("super-role"));
    }

    @Test
    void shouldUseMessageCodeForInvalidEmail() {
        doAnswer(invocation -> {
                    Errors errors = invocation.getArgument(1);
                    errors.rejectValue("email", "validation.error.email.pattern");
                    return null;
                })
                .when(validator)
                .validate(any(), any());
        webClient
                .post()
                .uri("/users/-/send-email-verification-code")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "invalid"))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("邮箱格式不正确");
    }

    @Nested
    class UserListTest {

        @Test
        void shouldListEmptyUsersWhenNoUsers() {
            when(roleService.getRolesByUsernames(any())).thenReturn(Mono.just(Map.of()));
            when(roleService.list(any())).thenReturn(Flux.empty());
            when(client.listBy(same(User.class), any(), any(PageRequest.class)))
                    .thenReturn(Mono.just(ListResult.emptyResult()));

            webClient
                    .get()
                    .uri("/users")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.items.length()")
                    .isEqualTo(0)
                    .jsonPath("$.total")
                    .isEqualTo(0);
        }

        @Test
        void shouldListUsersWhenUserPresent() {
            var users = List.of(createUser("fake-user-1"), createUser("fake-user-2"), createUser("fake-user-3"));
            var expectResult = new ListResult<>(users);
            when(roleService.getRolesByUsernames(any())).thenReturn(Mono.just(Map.of()));
            when(roleService.list(anySet())).thenReturn(Flux.empty());
            when(client.listBy(same(User.class), any(), any(PageRequest.class))).thenReturn(Mono.just(expectResult));

            webClient
                    .get()
                    .uri("/users")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.items.length()")
                    .isEqualTo(3)
                    .jsonPath("$.total")
                    .isEqualTo(3);
        }

        @Test
        void shouldRedactSecretsWhenListingUsers() {
            var user = createUser("fake-user-1");
            user.getSpec().setPassword("fake-password");
            user.getSpec().setTotpEncryptedSecret("encrypted-secret");
            when(roleService.getRolesByUsernames(any())).thenReturn(Mono.just(Map.of()));
            when(roleService.list(anySet())).thenReturn(Flux.empty());
            when(client.listBy(same(User.class), any(), any(PageRequest.class)))
                    .thenReturn(Mono.just(new ListResult<>(List.of(user))));

            webClient
                    .get()
                    .uri("/users")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.items[0].user.spec.password")
                    .doesNotExist()
                    .jsonPath("$.items[0].user.spec.totpEncryptedSecret")
                    .doesNotExist();
        }

        @Test
        void shouldFilterUsersWhenRoleProvided() {
            var expectUser = JsonUtils.jsonToObject("""
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
            var users = List.of(expectUser);
            var expectResult = new ListResult<>(users);
            when(client.listBy(same(User.class), any(), any(PageRequest.class))).thenReturn(Mono.just(expectResult));
            when(roleService.getRolesByUsernames(any())).thenReturn(Mono.just(Map.of()));
            when(roleService.list(anySet())).thenReturn(Flux.empty());

            webClient.get().uri("/users?role=guest").exchange().expectStatus().isOk();
        }

        @Test
        void shouldSortUsersWhenCreationTimestampSet() {
            var expectUser = createUser("fake-user-2", "expected display name");
            var expectResult = new ListResult<>(List.of(expectUser));
            when(client.listBy(same(User.class), any(), any(PageRequest.class))).thenReturn(Mono.just(expectResult));
            when(roleService.getRolesByUsernames(any())).thenReturn(Mono.just(Map.of()));
            when(roleService.list(anySet())).thenReturn(Flux.empty());

            webClient
                    .get()
                    .uri("/users?sort=creationTimestamp,desc")
                    .exchange()
                    .expectStatus()
                    .isOk();
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
    }

    @Nested
    @DisplayName("GetUserDetail")
    class GetUserDetailTest {

        @Test
        void shouldResponseErrorIfUserNotFound() {
            when(userService.getUser("fake-user")).thenReturn(Mono.error(new UserNotFoundException("fake-user")));
            webClient.get().uri("/users/-").exchange().expectStatus().isNotFound();

            verify(userService).getUser(eq("fake-user"));
        }

        @Test
        void shouldGetCurrentUserDetail() {
            var metadata = new Metadata();
            metadata.setName("fake-user");
            var user = new User();
            user.setMetadata(metadata);
            user.getSpec().setPassword("fake-encoded-password");
            user.getSpec().setTotpEncryptedSecret("encrypted-secret");
            when(userService.getUser("fake-user")).thenReturn(Mono.just(user));
            Role role = new Role();
            role.setMetadata(new Metadata());
            role.getMetadata().setName("super-role");
            role.setRules(List.of());
            when(roleService.list(Set.of("super-role"), true)).thenReturn(Flux.just(role));
            webClient
                    .get()
                    .uri("/users/-")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON)
                    .expectBody(UserEndpoint.DetailedUser.class)
                    .value(detail -> {
                        assertEquals(List.of(role), detail.roles());
                        assertEquals(true, detail.passwordSet());
                        assertEquals("fake-user", detail.user().getMetadata().getName());
                        assertNull(detail.user().getSpec().getPassword());
                        assertNull(detail.user().getSpec().getTotpEncryptedSecret());
                    });
        }

        @Test
        void shouldRedactSecretsWhenGettingAnotherUserDetail() {
            var metadata = new Metadata();
            metadata.setName("target");
            var user = new User();
            user.setMetadata(metadata);
            user.getSpec().setPassword("fake-encoded-password");
            user.getSpec().setTotpEncryptedSecret("encrypted-secret");
            when(userService.getUser("target")).thenReturn(Mono.just(user));
            when(roleService.getRolesByUsername("target")).thenReturn(Flux.empty());
            when(roleService.list(anySet(), eq(true))).thenReturn(Flux.empty());

            webClient
                    .get()
                    .uri("/users/target")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.user.spec.password")
                    .doesNotExist()
                    .jsonPath("$.user.spec.totpEncryptedSecret")
                    .doesNotExist();
        }

        @Test
        void shouldGetCurrentUserDetailWithImpersonator() {
            var metadata = new Metadata();
            metadata.setName("fake-user");
            var user = new User();
            user.setMetadata(metadata);
            when(userService.getUser("fake-user")).thenReturn(Mono.just(user));
            Role role = new Role();
            role.setMetadata(new Metadata());
            role.getMetadata().setName("fake-super-role");
            role.setRules(List.of());
            when(roleService.list(anySet(), eq(true))).thenReturn(Flux.just(role));

            var originalAuth = UsernamePasswordAuthenticationToken.authenticated(
                    "admin", "password", AuthorityUtils.createAuthorityList("ROLE_super-role"));
            var authorities = AuthorityUtils.createAuthorityList("ROLE_fake-super-role");
            authorities.add(
                    new SwitchUserGrantedAuthority(SwitchUserWebFilter.ROLE_PREVIOUS_ADMINISTRATOR, originalAuth));
            var impersonatedAuth =
                    UsernamePasswordAuthenticationToken.authenticated("fake-user", "password", authorities);

            webClient
                    .mutateWith(mockAuthentication(impersonatedAuth))
                    .get()
                    .uri("/users/-")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(UserEndpoint.DetailedUser.class)
                    .isEqualTo(new UserEndpoint.DetailedUser(user, List.of(role), "admin", false));
        }
    }

    @Nested
    @DisplayName("UpdateProfile")
    class UpdateProfileTest {

        @Test
        void shouldUpdateProfileCorrectly() {
            var currentUser = createUser("fake-user");
            var updatedUser = createUser("fake-user");

            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));
            when(client.update(currentUser)).thenReturn(Mono.just(updatedUser));
            when(environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                    .thenReturn(Mono.just(new SystemSetting.User()));

            webClient
                    .put()
                    .uri("/users/-")
                    .bodyValue(Map.of("displayName", "Updated", "bio", "Updated bio"))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(User.class)
                    .value(user -> {
                        assertEquals("fake-user", user.getMetadata().getName());
                        assertEquals(
                                updatedUser.getSpec().getDisplayName(),
                                user.getSpec().getDisplayName());
                        assertEquals(
                                updatedUser.getSpec().getBio(), user.getSpec().getBio());
                        assertNull(user.getSpec().getPassword());
                        assertNull(user.getSpec().getTotpEncryptedSecret());
                    });

            verify(client).get(User.class, "fake-user");
            verify(client).update(currentUser);
        }

        @Test
        void shouldPreserveSensitiveFieldsWhenUpdatingProfile() {
            var currentUser = createUser("fake-user");
            currentUser.getSpec().setPhone("123");
            currentUser.getSpec().setTwoFactorAuthEnabled(true);
            currentUser.getSpec().setTotpEncryptedSecret("configured-secret");
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));
            when(client.update(currentUser)).thenReturn(Mono.just(currentUser));
            when(environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                    .thenReturn(Mono.just(new SystemSetting.User()));

            webClient
                    .put()
                    .uri("/users/-")
                    .bodyValue(Map.of(
                            "metadata",
                            Map.of("name", "another-user"),
                            "spec",
                            Map.of(
                                    "email", "attacker@example.com",
                                    "password", "attacker-password",
                                    "phone", "456",
                                    "twoFactorAuthEnabled", false,
                                    "totpEncryptedSecret", "attacker-secret"),
                            "displayName",
                            "Updated",
                            "bio",
                            "Updated bio"))
                    .exchange()
                    .expectStatus()
                    .isOk();

            assertEquals("Updated", currentUser.getSpec().getDisplayName());
            assertEquals("Updated bio", currentUser.getSpec().getBio());
            assertEquals("hi@halo.run", currentUser.getSpec().getEmail());
            assertEquals("fake-password", currentUser.getSpec().getPassword());
            assertEquals("123", currentUser.getSpec().getPhone());
            assertEquals(true, currentUser.getSpec().getTwoFactorAuthEnabled());
            assertEquals("configured-secret", currentUser.getSpec().getTotpEncryptedSecret());
        }

        @ParameterizedTest
        @ValueSource(strings = {"{}", "{\"displayName\": null}", "{\"displayName\": \" \"}"})
        void shouldRejectBlankDisplayName(String body) {
            var currentUser = createUser("fake-user");
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));

            webClient
                    .put()
                    .uri("/users/-")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .expectStatus()
                    .isBadRequest();

            assertEquals("Faker", currentUser.getSpec().getDisplayName());
            verify(client, never()).update(any(User.class));
        }

        @Test
        void shouldRejectDisplayNameExceedingMaxLength() {
            var currentUser = createUser("fake-user");
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));

            webClient
                    .put()
                    .uri("/users/-")
                    .bodyValue(Map.of("displayName", "a".repeat(51)))
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .jsonPath("$.detail")
                    .isEqualTo("The display name is too long.");

            verify(client, never()).update(any(User.class));
        }

        @Test
        void shouldAllowUpdatingOtherFieldsWhenExistingDisplayNameExceedsMaxLength() {
            var currentUser = createUser("fake-user");
            currentUser.getSpec().setDisplayName("a".repeat(51));
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));
            when(client.update(currentUser)).thenReturn(Mono.just(currentUser));

            webClient
                    .put()
                    .uri("/users/-")
                    .bodyValue(Map.of("displayName", "a".repeat(51), "bio", "Updated bio"))
                    .exchange()
                    .expectStatus()
                    .isOk();

            assertEquals("Updated bio", currentUser.getSpec().getBio());
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
            when(userService.updateWithRawPassword("fake-user", "new-password")).thenReturn(Mono.just(user));
            when(userService.confirmPassword("fake-user", "old-password")).thenReturn(Mono.just(true));
            webClient
                    .mutateWith(mockUser("fake-user").roles("role-template-manage-users"))
                    .put()
                    .uri("/users/-/password")
                    .bodyValue(new UserEndpoint.ChangeOwnPasswordRequest("old-password", "new-password"))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(User.class)
                    .isEqualTo(user);

            verify(userService, times(1)).updateWithRawPassword("fake-user", "new-password");
        }

        @Test
        void shouldUpdateOtherPasswordCorrectly() {
            var user = new User();
            when(userService.updateWithRawPassword("another-fake-user", "new-password"))
                    .thenReturn(Mono.just(user));
            webClient
                    .put()
                    .uri("/users/another-fake-user/password")
                    .bodyValue(new UserEndpoint.ChangeOwnPasswordRequest("old-password", "new-password"))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(User.class)
                    .isEqualTo(user);

            verify(userService, times(1)).updateWithRawPassword("another-fake-user", "new-password");
        }
    }

    @Nested
    @DisplayName("GrantPermission")
    class GrantPermissionEndpointTest {

        @Test
        void shouldGetBadRequestIfRequestBodyIsEmpty() {
            webClient
                    .post()
                    .uri("/users/fake-user/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus()
                    .isBadRequest();

            // Why one more time to verify? Because the SuperAdminInitializer will fetch admin user.
            verify(client, never()).fetch(same(User.class), eq("fake-user"));
            verify(client, never()).fetch(same(Role.class), eq("fake-role"));
        }

        @Test
        void shouldGrantPermission() {
            when(userService.grantRoles("fake-user", Set.of("fake-role"))).thenReturn(Mono.empty());

            webClient
                    .post()
                    .uri("/users/fake-user/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new UserEndpoint.GrantRequest(Set.of("fake-role")))
                    .exchange()
                    .expectStatus()
                    .isOk();
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
                            "rbac.authorization.halo.run/ui-permissions": \
                            "[\\"permission-A\\", \\"permission-A\\"]"
                        }
                    },
                    "rules": []
                }
                """, Role.class);
            when(roleService.listPermissions(eq(Set.of("test-A")))).thenReturn(Flux.just(roleA));
            when(roleService.getRolesByUsername("fake-user")).thenReturn(Flux.just("test-A"));
            when(roleService.list(Set.of("test-A"), true)).thenReturn(Flux.just(roleA));

            webClient
                    .get()
                    .uri("/users/fake-user/permissions")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(UserEndpoint.UserPermission.class)
                    .value(userPermission -> {
                        assertEquals(List.of(roleA), userPermission.getRoles());
                        assertEquals(List.of(roleA), userPermission.getPermissions());
                        assertEquals(List.of("permission-A"), userPermission.getUiPermissions());
                    });
        }
    }

    @Test
    void shouldCreateUserWithConfiguredDefaultRole() {
        var setting = new SystemSetting.User();
        setting.setDefaultRole("guest");
        when(environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(userService.createUser(any(User.class), anySet())).thenReturn(Mono.just(new User()));
        var userRequest = new UserEndpoint.CreateUserRequest("fake-user", "fake-email", "", "", "", "", "", Map.of());
        webClient
                .post()
                .uri("/users")
                .bodyValue(userRequest)
                .exchange()
                .expectStatus()
                .isOk();
        verify(userService).createUser(any(User.class), eq(Set.of("guest")));
    }

    @Test
    void shouldIgnoreRequestedRolesWhenCreatingUser() {
        var setting = new SystemSetting.User();
        setting.setDefaultRole("guest");
        when(environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(userService.createUser(any(User.class), eq(Set.of("guest")))).thenReturn(Mono.just(new User()));
        webClient
                .post()
                .uri("/users")
                .bodyValue(Map.of(
                        "name",
                        "new-user",
                        "email",
                        "user@example.com",
                        "password",
                        "password",
                        "roles",
                        List.of("super-role")))
                .exchange()
                .expectStatus()
                .isOk();
        verify(userService).createUser(any(User.class), eq(Set.of("guest")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "super-role"})
    void shouldRejectCreationWithInvalidDefaultRole(String role) {
        var setting = new SystemSetting.User();
        setting.setDefaultRole(role);
        when(environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        webClient
                .post()
                .uri("/users")
                .bodyValue(Map.of("name", "new-user", "email", "user@example.com", "password", "password"))
                .exchange()
                .expectStatus()
                .isBadRequest();
        verify(userService, never()).createUser(any(User.class), anySet());
    }

    @Test
    void shouldRejectCreateWhenEmailIsBlank() {
        var userRequest = new UserEndpoint.CreateUserRequest("fake-user", " ", "", "", "", "", "", Map.of());

        webClient
                .post()
                .uri("/users")
                .bodyValue(userRequest)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("Email is required");

        verify(userService, never()).createUser(any(User.class), anySet());
    }

    @Test
    void shouldRejectCreateWhenDisplayNameIsTooLong() {
        var userRequest = new UserEndpoint.CreateUserRequest(
                "fake-user", "fake-email", "a".repeat(51), "", "", "password", "", Map.of());

        webClient
                .post()
                .uri("/users")
                .bodyValue(userRequest)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("The display name is too long.");

        verify(userService, never()).createUser(any(User.class), anySet());
    }

    @Test
    void shouldRejectCreateWhenUsernameIsTooLong() {
        var userRequest = new UserEndpoint.CreateUserRequest(
                "a".repeat(64), "fake-email", "display name", "", "", "password", "", Map.of());

        webClient
                .post()
                .uri("/users")
                .bodyValue(userRequest)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("The username is too long.");

        verify(userService, never()).createUser(any(User.class), anySet());
    }

    @ParameterizedTest
    @ValueSource(strings = {"role-template-manage-users", "role-template-manage-permissions"})
    void shouldRejectRoleAssignmentAndPasswordResetForNonSuperUser(String role) {
        var delegated = webClient.mutateWith(mockUser("manager").roles(role));
        delegated
                .post()
                .uri("/users/manager/permissions")
                .bodyValue(Map.of("roles", List.of("super-role")))
                .exchange()
                .expectStatus()
                .isForbidden();
        delegated
                .put()
                .uri("/users/admin/password")
                .bodyValue(Map.of("password", "new-password"))
                .exchange()
                .expectStatus()
                .isForbidden();
        delegated
                .put()
                .uri("/users/manager/password")
                .bodyValue(Map.of("password", "new-password"))
                .exchange()
                .expectStatus()
                .isForbidden();
        verify(userService, never()).grantRoles(anyString(), anySet());
        verify(userService, never()).updateWithRawPassword(anyString(), anyString());
    }

    @Test
    void shouldUpdateOnlyProfileFieldsForDelegatedManager() {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("target");
        user.getMetadata().setAnnotations(Map.of(User.EMAIL_TO_VERIFY, "pending@example.com"));
        user.getSpec().setEmail("target@example.com");
        user.getSpec().setPassword("original-hash");
        user.getSpec().setEmailVerified(true);
        user.getSpec().setTwoFactorAuthEnabled(true);
        user.getSpec().setTotpEncryptedSecret("original-secret");
        when(client.get(User.class, "target")).thenReturn(Mono.just(user));
        when(client.update(user)).thenReturn(Mono.just(user));

        webClient
                .mutateWith(mockUser("manager").roles("role-template-manage-users"))
                .put()
                .uri("/users/target")
                .bodyValue(Map.of(
                        "displayName",
                        "Updated",
                        "email",
                        "target@example.com",
                        "bio",
                        "Biography",
                        "password",
                        "attacker-password",
                        "emailVerified",
                        false,
                        "twoFactorAuthEnabled",
                        false,
                        "totpEncryptedSecret",
                        "attacker-secret",
                        "roles",
                        List.of("super-role"),
                        "annotations",
                        Map.of("custom", "value", User.EMAIL_TO_VERIFY, "attacker@example.com")))
                .exchange()
                .expectStatus()
                .isOk();

        assertEquals("Updated", user.getSpec().getDisplayName());
        assertEquals("Biography", user.getSpec().getBio());
        assertEquals("original-hash", user.getSpec().getPassword());
        assertEquals(true, user.getSpec().isEmailVerified());
        assertEquals(true, user.getSpec().getTwoFactorAuthEnabled());
        assertEquals("original-secret", user.getSpec().getTotpEncryptedSecret());
        assertEquals(
                Map.of("custom", "value", User.EMAIL_TO_VERIFY, "pending@example.com"),
                user.getMetadata().getAnnotations());
        verify(userService, never()).grantRoles(anyString(), anySet());
    }

    @Test
    void shouldRejectUpdatingDisplayNameExceedingMaxLength() {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("target");
        user.getSpec().setEmail("target@example.com");
        when(client.get(User.class, "target")).thenReturn(Mono.just(user));

        webClient
                .put()
                .uri("/users/target")
                .bodyValue(Map.of("displayName", "a".repeat(51), "email", "target@example.com"))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("The display name is too long.");

        verify(client, never()).update(any(User.class));
    }

    @Test
    void shouldAllowUpdatingOtherFieldsWhenExistingDisplayNameExceedsMaxLength() {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("target");
        user.getSpec().setEmail("target@example.com");
        user.getSpec().setDisplayName("a".repeat(51));
        when(client.get(User.class, "target")).thenReturn(Mono.just(user));
        when(client.update(user)).thenReturn(Mono.just(user));

        webClient
                .put()
                .uri("/users/target")
                .bodyValue(Map.of("displayName", "a".repeat(51), "email", "target@example.com", "bio", "Updated bio"))
                .exchange()
                .expectStatus()
                .isOk();

        assertEquals("Updated bio", user.getSpec().getBio());
    }

    @Test
    void shouldRedactSecretsWhenRespondingToUserUpdates() {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("target");
        user.getSpec().setEmail("target@example.com");
        user.getSpec().setPassword("fake-encoded-password");
        user.getSpec().setTotpEncryptedSecret("encrypted-secret");
        when(client.get(User.class, "target")).thenReturn(Mono.just(user));
        when(client.update(user)).thenReturn(Mono.just(user));

        webClient
                .put()
                .uri("/users/target")
                .bodyValue(Map.of("displayName", "Updated", "email", "target@example.com", "bio", "Biography"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.spec.password")
                .doesNotExist()
                .jsonPath("$.spec.totpEncryptedSecret")
                .doesNotExist();
    }

    @Test
    void shouldRedactSecretsWhenChangingAnyonePasswordForAdmin() {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("target");
        user.getSpec().setPassword("fake-encoded-password");
        user.getSpec().setTotpEncryptedSecret("encrypted-secret");
        when(userService.updateWithRawPassword("target", "new-password")).thenReturn(Mono.just(user));

        webClient
                .put()
                .uri("/users/target/password")
                .bodyValue(Map.of("password", "new-password"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.spec.password")
                .doesNotExist()
                .jsonPath("$.spec.totpEncryptedSecret")
                .doesNotExist();
    }

    @Test
    void shouldAllowOnlySuperAdministratorToChangeEmail() {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("target");
        user.getSpec().setEmail("original@example.com");
        when(client.get(User.class, "target")).thenReturn(Mono.just(user));
        var body = Map.of("displayName", "Target", "email", "updated@example.com");
        webClient
                .mutateWith(mockUser("manager").roles("role-template-manage-users"))
                .put()
                .uri("/users/target")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isForbidden();
        verify(client, never()).update(any());
        assertEquals("original@example.com", user.getSpec().getEmail());

        when(client.update(user)).thenReturn(Mono.just(user));
        webClient
                .put()
                .uri("/users/target")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isOk();
        assertEquals("updated@example.com", user.getSpec().getEmail());
    }

    @Nested
    class AvatarDeleteTest {

        @Test
        void shouldDeleteAvatarAndRedactSecrets() {
            var metadata = new Metadata();
            metadata.setName("fake-user");
            metadata.setAnnotations(new HashMap<>(Map.of(User.AVATAR_ATTACHMENT_NAME_ANNO, "fake-attachment")));
            var user = new User();
            user.setMetadata(metadata);
            user.getSpec().setAvatar("fake-avatar.png");
            user.getSpec().setPassword("fake-password");
            user.getSpec().setTotpEncryptedSecret("encrypted-secret");

            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(user));
            when(client.update(user)).thenReturn(Mono.just(user));

            webClient
                    .delete()
                    .uri("/users/-/avatar")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.spec.avatar")
                    .doesNotExist()
                    .jsonPath("$.metadata.annotations['halo.run/avatar-attachment-name']")
                    .doesNotExist()
                    .jsonPath("$.spec.password")
                    .doesNotExist()
                    .jsonPath("$.spec.totpEncryptedSecret")
                    .doesNotExist();

            verify(client).update(user);
        }
    }

    @Nested
    class AvatarUploadTest {
        @Test
        void respondWithErrorIfExtensionNotAllowed() {

            var multipartBodyBuilder = new MultipartBodyBuilder();
            multipartBodyBuilder
                    .part("file", "fake-file")
                    .contentType(MediaType.parseMediaType("image/bmp"))
                    .filename("fake-filename.bmp");

            when(environmentFetcher.fetch(SystemSetting.Attachment.GROUP, SystemSetting.Attachment.class))
                    .thenReturn(Mono.fromSupplier(() ->
                            SystemSetting.Attachment.builder().avatar(null).build()));

            webClient
                    .post()
                    .uri("/users/-/avatar")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                    .exchange()
                    .expectStatus()
                    .is4xxClientError();
        }

        @ParameterizedTest
        @ValueSource(strings = {"png", "jpg", "jpeg", "gif"})
        void shouldAllowAllowedExtensions(String ext) {
            var currentUser = createUser("fake-user");

            Attachment attachment = new Attachment();
            Metadata metadata = new Metadata();
            metadata.setName("fake-attachment");
            attachment.setMetadata(metadata);

            var multipartBodyBuilder = new MultipartBodyBuilder();
            multipartBodyBuilder
                    .part("file", "fake-file")
                    .contentType(MediaType.parseMediaType("image/" + ext))
                    .filename("fake-filename." + ext);

            when(environmentFetcher.fetch(SystemSetting.Attachment.GROUP, SystemSetting.Attachment.class))
                    .thenReturn(Mono.fromSupplier(() ->
                            SystemSetting.Attachment.builder().avatar(null).build()));

            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));
            when(attachmentService.upload(eq("default-policy"), anyString(), anyString(), any(), any(MediaType.class)))
                    .thenReturn(Mono.just(attachment));
            when(client.update(currentUser)).thenReturn(Mono.just(currentUser));

            webClient
                    .post()
                    .uri("/users/-/avatar")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.spec.avatar")
                    .isEqualTo("fake-avatar.png")
                    .jsonPath("$.metadata.annotations['halo.run/avatar-attachment-name']")
                    .isEqualTo("fake-attachment")
                    .jsonPath("$.spec.password")
                    .doesNotExist()
                    .jsonPath("$.spec.totpEncryptedSecret")
                    .doesNotExist();

            verify(client).get(User.class, "fake-user");
            verify(client).update(currentUser);
        }

        @Test
        void shouldUploadSuccessfully() {
            var currentUser = createUser("fake-user");

            Attachment attachment = new Attachment();
            Metadata metadata = new Metadata();
            metadata.setName("fake-attachment");
            attachment.setMetadata(metadata);

            var multipartBodyBuilder = new MultipartBodyBuilder();
            multipartBodyBuilder
                    .part("file", "fake-file")
                    .contentType(MediaType.IMAGE_PNG)
                    .filename("fake-filename.png");

            when(environmentFetcher.fetch(SystemSetting.Attachment.GROUP, SystemSetting.Attachment.class))
                    .thenReturn(Mono.fromSupplier(() ->
                            SystemSetting.Attachment.builder().avatar(null).build()));

            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));
            when(attachmentService.upload(
                            eq("default-policy"), anyString(), anyString(), any(), any(MediaType.IMAGE_PNG.getClass())))
                    .thenReturn(Mono.just(attachment));

            when(client.update(currentUser)).thenReturn(Mono.just(currentUser));

            webClient
                    .post()
                    .uri("/users/-/avatar")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.spec.avatar")
                    .isEqualTo("fake-avatar.png")
                    .jsonPath("$.metadata.annotations['halo.run/avatar-attachment-name']")
                    .isEqualTo("fake-attachment")
                    .jsonPath("$.spec.password")
                    .doesNotExist()
                    .jsonPath("$.spec.totpEncryptedSecret")
                    .doesNotExist();

            verify(client).get(User.class, "fake-user");
            verify(client).update(currentUser);
        }

        @Test
        void shouldUseAttachmentSetting() {
            var currentUser = createUser("fake-user");

            Attachment attachment = new Attachment();
            Metadata metadata = new Metadata();
            metadata.setName("fake-attachment");
            attachment.setMetadata(metadata);

            var multipartBodyBuilder = new MultipartBodyBuilder();
            multipartBodyBuilder
                    .part("file", "fake-file")
                    .contentType(MediaType.IMAGE_PNG)
                    .filename("fake-filename.png");

            when(environmentFetcher.fetch(SystemSetting.Attachment.GROUP, SystemSetting.Attachment.class))
                    .thenReturn(Mono.fromSupplier(() -> SystemSetting.Attachment.builder()
                            .avatar(SystemSetting.Attachment.UploadOptions.builder()
                                    .policyName("fake-avatar-policy")
                                    .build())
                            .build()));

            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(currentUser));
            when(attachmentService.upload(
                            eq("fake-avatar-policy"),
                            anyString(),
                            anyString(),
                            any(),
                            any(MediaType.IMAGE_PNG.getClass())))
                    .thenReturn(Mono.just(attachment));

            when(client.update(currentUser)).thenReturn(Mono.just(currentUser));

            webClient
                    .post()
                    .uri("/users/-/avatar")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.spec.avatar")
                    .isEqualTo("fake-avatar.png")
                    .jsonPath("$.metadata.annotations['halo.run/avatar-attachment-name']")
                    .isEqualTo("fake-attachment")
                    .jsonPath("$.spec.password")
                    .doesNotExist()
                    .jsonPath("$.spec.totpEncryptedSecret")
                    .doesNotExist();

            verify(client).get(User.class, "fake-user");
            verify(client).update(currentUser);
        }

        User createUser(String name) {
            var spec = new User.UserSpec();
            spec.setEmail("hi@halo.run");
            spec.setBio("Fake bio");
            spec.setDisplayName("Faker");
            spec.setAvatar("fake-avatar.png");
            spec.setPassword("fake-password");

            var metadata = new Metadata();
            metadata.setName(name);
            metadata.setAnnotations(new HashMap<>());

            var user = new User();
            user.setSpec(spec);
            user.setMetadata(metadata);
            return user;
        }
    }
}
