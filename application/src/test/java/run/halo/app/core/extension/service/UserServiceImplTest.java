package run.halo.app.core.extension.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.GroupVersionKind.fromExtension;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.DuplicateNameException;
import run.halo.app.infra.exception.UserNotFoundException;
import run.halo.app.infra.utils.JsonUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void shouldThrowExceptionIfUserNotFoundInExtension() {
        when(client.get(eq(User.class), eq("faker"))).thenReturn(
            Mono.error(new ExtensionNotFoundException(fromExtension(User.class), "faker")));
        StepVerifier.create(userService.getUser("faker"))
            .verifyError(UserNotFoundException.class);

        verify(client, times(1)).get(eq(User.class), eq("faker"));
    }

    @Test
    void shouldGetUserIfUserFoundInExtension() {
        User fakeUser = new User();
        when(client.get(User.class, "faker")).thenReturn(Mono.just(fakeUser));

        StepVerifier.create(userService.getUser("faker"))
            .assertNext(user -> assertEquals(fakeUser, user))
            .verifyComplete();

        verify(client, times(1)).get(eq(User.class), eq("faker"));
    }

    @Test
    void shouldUpdatePasswordIfUserFoundInExtension() {
        var fakeUser = new User();
        fakeUser.setSpec(new User.UserSpec());

        when(client.get(User.class, "faker")).thenReturn(Mono.just(fakeUser));
        when(client.update(eq(fakeUser))).thenReturn(Mono.just(fakeUser));

        StepVerifier.create(userService.updatePassword("faker", "new-fake-password"))
            .expectNext(fakeUser)
            .verifyComplete();

        verify(client, times(1)).get(eq(User.class), eq("faker"));
        verify(client, times(1)).update(argThat(extension -> {
            var user = (User) extension;
            return "new-fake-password".equals(user.getSpec().getPassword());
        }));
    }

    @Test
    void shouldListRolesIfUserFoundInExtension() {
        User fakeUser = new User();
        Metadata metadata = new Metadata();
        metadata.setName("faker");
        fakeUser.setMetadata(metadata);
        fakeUser.setSpec(new User.UserSpec());

        when(client.list(eq(RoleBinding.class), any(), any())).thenReturn(
            Flux.fromIterable(getRoleBindings()));
        Role roleA = new Role();
        Metadata metadataA = new Metadata();
        metadataA.setName("test-A");
        roleA.setMetadata(metadataA);

        Role roleB = new Role();
        Metadata metadataB = new Metadata();
        metadataB.setName("test-B");
        roleB.setMetadata(metadataB);

        Role roleC = new Role();
        Metadata metadataC = new Metadata();
        metadataC.setName("ddd");
        roleC.setMetadata(metadataC);

        when(client.fetch(eq(Role.class), eq("test-A"))).thenReturn(Mono.just(roleA));
        when(client.fetch(eq(Role.class), eq("test-B"))).thenReturn(Mono.just(roleB));
        lenient().when(client.fetch(eq(Role.class), eq("ddd"))).thenReturn(Mono.just(roleC));

        StepVerifier.create(userService.listRoles("faker"))
            .expectNext(roleA)
            .expectNext(roleB)
            .verifyComplete();

        verify(client, times(1)).list(eq(RoleBinding.class), any(), any());

        verify(client, times(1)).fetch(eq(Role.class), eq("test-A"));
        verify(client, times(1)).fetch(eq(Role.class), eq("test-B"));
        verify(client, times(0)).fetch(eq(Role.class), eq("ddd"));
    }

    List<RoleBinding> getRoleBindings() {
        String bindA = """
            {
               
                "apiVersion": "v1alpha1",
                "kind": "RoleBinding",
                 "metadata": {
                    "name": "bind-A"
                },
                 "subjects": [{
                        "kind": "User",
                        "name": "faker",
                        "apiGroup": ""
                 }],
                "roleRef": {
                    "kind": "Role",
                    "name": "test-A",
                    "apiGroup": ""
                }
            }
            """;

        String bindB = """
            {
                
                "apiVersion": "v1alpha1",
                "kind": "RoleBinding",
                "metadata": {
                    "name": "bind-B"
                },
                "subjects": [{
                        "kind": "User",
                        "name": "faker",
                        "apiGroup": ""
                    },
                    {
                        "kind": "User",
                        "name": "zhangsan",
                        "apiGroup": ""
                }],
                "roleRef": {
                    "kind": "Role",
                    "name": "test-B",
                    "apiGroup": ""
                }
            }
            """;

        String bindC = """
            {
                "apiVersion": "v1alpha1",
                "kind": "RoleBinding",
                "metadata": {
                    "name": "bind-C"
                },
                "subjects": [{
                        "kind": "User",
                        "name": "faker",
                        "apiGroup": ""
                }],
                "roleRef": {
                    "kind": "Fake",
                    "name": "ddd",
                    "apiGroup": ""
                }
            }
            """;
        return List.of(JsonUtils.jsonToObject(bindA, RoleBinding.class),
            JsonUtils.jsonToObject(bindB, RoleBinding.class),
            JsonUtils.jsonToObject(bindC, RoleBinding.class));
    }

    @Nested
    @DisplayName("UpdateWithRawPassword")
    class UpdateWithRawPasswordTest {

        @Test
        void shouldUpdatePasswordWithDifferentPassword() {
            var oldUser = createUser("fake-password");
            var newUser = createUser("new-password");

            when(client.get(User.class, "fake-user")).thenReturn(
                Mono.just(oldUser));
            when(client.update(eq(oldUser))).thenReturn(Mono.just(newUser));
            when(passwordEncoder.matches("new-password", "fake-password")).thenReturn(false);
            when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

            StepVerifier.create(userService.updateWithRawPassword("fake-user", "new-password"))
                .expectNext(newUser)
                .verifyComplete();

            verify(passwordEncoder).matches("new-password", "fake-password");
            verify(passwordEncoder).encode("new-password");
            verify(client).get(User.class, "fake-user");
            verify(client).update(argThat(extension -> {
                var user = (User) extension;
                return "encoded-new-password".equals(user.getSpec().getPassword());
            }));
        }

        @Test
        void shouldUpdatePasswordIfNoPasswordBefore() {
            var oldUser = createUser(null);
            var newUser = createUser("new-password");

            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(oldUser));
            when(client.update(oldUser)).thenReturn(Mono.just(newUser));
            when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

            StepVerifier.create(userService.updateWithRawPassword("fake-user", "new-password"))
                .expectNext(newUser)
                .verifyComplete();

            verify(passwordEncoder, never()).matches("new-password", null);
            verify(passwordEncoder).encode("new-password");
            verify(client).update(argThat(extension -> {
                var user = (User) extension;
                return "encoded-new-password".equals(user.getSpec().getPassword());
            }));
            verify(client).get(User.class, "fake-user");
        }

        @Test
        void shouldDoNothingIfPasswordNotChanged() {
            userService = spy(userService);

            var oldUser = createUser("fake-password");
            var newUser = createUser("new-password");
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(oldUser));
            when(passwordEncoder.matches("fake-password", "fake-password")).thenReturn(true);

            StepVerifier.create(userService.updateWithRawPassword("fake-user", "fake-password"))
                .expectNextCount(0)
                .verifyComplete();

            verify(passwordEncoder, times(1)).matches("fake-password", "fake-password");
            verify(passwordEncoder, never()).encode(any());
            verify(client, never()).update(any());
            verify(client).get(User.class, "fake-user");
        }

        @Test
        void shouldThrowExceptionIfUserNotFound() {
            when(client.get(eq(User.class), eq("fake-user")))
                .thenReturn(Mono.error(
                    new ExtensionNotFoundException(fromExtension(User.class), "fake-user")));

            StepVerifier.create(userService.updateWithRawPassword("fake-user", "new-password"))
                .verifyError(UserNotFoundException.class);

            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(passwordEncoder, never()).encode(anyString());
            verify(client, never()).update(any());
            verify(client).get(User.class, "fake-user");
        }

    }

    User createUser(String password) {
        var user = new User();
        user.setSpec(new User.UserSpec());
        user.getSpec().setPassword(password);
        return user;
    }

    @Nested
    class GrantRolesTest {

        @Test
        void shouldGetNotFoundIfUserNotFound() {
            when(client.get(User.class, "invalid-user"))
                .thenReturn(Mono.error(
                    new ExtensionNotFoundException(fromExtension(User.class), "invalid-user")));

            var grantRolesMono = userService.grantRoles("invalid-user", Set.of("fake-role"));
            StepVerifier.create(grantRolesMono)
                .expectError(ExtensionNotFoundException.class)
                .verify();

            verify(client).get(User.class, "invalid-user");
        }

        @Test
        void shouldCreateRoleBindingIfNotExist() {
            when(client.get(User.class, "fake-user"))
                .thenReturn(Mono.just(createUser("fake-password")));
            when(client.list(same(RoleBinding.class), any(), any())).thenReturn(Flux.empty());
            when(client.create(isA(RoleBinding.class))).thenReturn(
                Mono.just(mock(RoleBinding.class)));

            var grantRolesMono = userService.grantRoles("fake-user", Set.of("fake-role"));
            StepVerifier.create(grantRolesMono)
                .expectNextCount(1)
                .verifyComplete();

            verify(client).get(User.class, "fake-user");
            verify(client).list(same(RoleBinding.class), any(), any());
            verify(client).create(isA(RoleBinding.class));
        }

        @Test
        void shouldDeleteRoleBindingIfNotProvided() {
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(mock(User.class)));
            var notProvidedRoleBinding = RoleBinding.create("fake-user", "non-provided-fake-role");
            var existingRoleBinding = RoleBinding.create("fake-user", "fake-role");
            when(client.list(same(RoleBinding.class), any(), any())).thenReturn(
                Flux.fromIterable(List.of(notProvidedRoleBinding, existingRoleBinding)));
            when(client.delete(isA(RoleBinding.class)))
                .thenReturn(Mono.just(mock(RoleBinding.class)));

            StepVerifier.create(userService.grantRoles("fake-user", Set.of("fake-role")))
                .expectNextCount(1)
                .verifyComplete();

            verify(client).get(User.class, "fake-user");
            verify(client).list(same(RoleBinding.class), any(), any());
            verify(client).delete(notProvidedRoleBinding);
        }

        @Test
        void shouldUpdateRoleBindingIfExists() {
            when(client.get(User.class, "fake-user")).thenReturn(Mono.just(mock(User.class)));
            // add another subject
            var anotherSubject = new RoleBinding.Subject();
            anotherSubject.setName("another-fake-user");
            anotherSubject.setKind(User.KIND);
            anotherSubject.setApiGroup(User.GROUP);
            var notProvidedRoleBinding = RoleBinding.create("fake-user", "non-provided-fake-role");
            notProvidedRoleBinding.getSubjects().add(anotherSubject);

            var existingRoleBinding = RoleBinding.create("fake-user", "fake-role");

            when(client.list(same(RoleBinding.class), any(), any())).thenReturn(
                Flux.fromIterable(List.of(notProvidedRoleBinding, existingRoleBinding)));
            when(client.update(isA(RoleBinding.class)))
                .thenReturn(Mono.just(mock(RoleBinding.class)));

            StepVerifier.create(userService.grantRoles("fake-user", Set.of("fake-role")))
                .expectNextCount(1)
                .verifyComplete();

            verify(client).get(User.class, "fake-user");
            verify(client).list(same(RoleBinding.class), any(), any());
            verify(client).update(notProvidedRoleBinding);
        }
    }


    @Nested
    class SignUpTest {
        @Test
        void signUpWhenRegistrationNotAllowed() {
            SystemSetting.User userSetting = new SystemSetting.User();
            userSetting.setAllowRegistration(false);
            when(environmentFetcher.fetch(eq(SystemSetting.User.GROUP),
                eq(SystemSetting.User.class)))
                .thenReturn(Mono.just(userSetting));

            User fakeUser = fakeSignUpUser("fake-user", "fake-password");

            userService.signUp(fakeUser, "fake-password")
                .as(StepVerifier::create)
                .expectError(AccessDeniedException.class)
                .verify();
        }

        @Test
        void signUpWhenRegistrationDefaultRoleNotConfigured() {
            SystemSetting.User userSetting = new SystemSetting.User();
            userSetting.setAllowRegistration(true);
            when(environmentFetcher.fetch(eq(SystemSetting.User.GROUP),
                eq(SystemSetting.User.class)))
                .thenReturn(Mono.just(userSetting));

            User fakeUser = fakeSignUpUser("fake-user", "fake-password");

            userService.signUp(fakeUser, "fake-password")
                .as(StepVerifier::create)
                .expectError(AccessDeniedException.class)
                .verify();
        }

        @Test
        void signUpWhenRegistrationUsernameExists() {
            SystemSetting.User userSetting = new SystemSetting.User();
            userSetting.setAllowRegistration(true);
            userSetting.setDefaultRole("fake-role");
            when(environmentFetcher.fetch(eq(SystemSetting.User.GROUP),
                eq(SystemSetting.User.class)))
                .thenReturn(Mono.just(userSetting));
            when(passwordEncoder.encode(eq("fake-password"))).thenReturn("fake-password");
            when(client.fetch(eq(User.class), eq("fake-user")))
                .thenReturn(Mono.just(fakeSignUpUser("test", "test")));

            User fakeUser = fakeSignUpUser("fake-user", "fake-password");

            userService.signUp(fakeUser, "fake-password")
                .as(StepVerifier::create)
                .expectError(DuplicateNameException.class)
                .verify();
        }

        @Test
        void signUpWhenRegistrationSuccessfully() {
            SystemSetting.User userSetting = new SystemSetting.User();
            userSetting.setAllowRegistration(true);
            userSetting.setDefaultRole("fake-role");
            when(environmentFetcher.fetch(eq(SystemSetting.User.GROUP),
                eq(SystemSetting.User.class)))
                .thenReturn(Mono.just(userSetting));
            when(passwordEncoder.encode(eq("fake-password"))).thenReturn("fake-password");
            when(client.fetch(eq(User.class), eq("fake-user")))
                .thenReturn(Mono.empty());

            User fakeUser = fakeSignUpUser("fake-user", "fake-password");

            when(client.fetch(eq(Role.class), anyString())).thenReturn(Mono.just(new Role()));
            when(client.create(any(User.class))).thenReturn(Mono.just(fakeUser));
            UserServiceImpl spyUserService = spy(userService);
            doReturn(Mono.just(fakeUser)).when(spyUserService).grantRoles(eq("fake-user"),
                anySet());

            spyUserService.signUp(fakeUser, "fake-password")
                .as(StepVerifier::create)
                .consumeNextWith(user -> {
                    assertThat(user.getMetadata().getName()).isEqualTo("fake-user");
                    assertThat(user.getSpec().getPassword()).isEqualTo("fake-password");
                })
                .verifyComplete();

            verify(client).create(any(User.class));
            verify(spyUserService).grantRoles(eq("fake-user"), anySet());
        }

        User fakeSignUpUser(String name, String password) {
            User user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName(name);
            user.setSpec(new User.UserSpec());
            user.getSpec().setPassword(password);
            return user;
        }
    }
}
