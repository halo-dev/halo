package run.halo.app.core.extension.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.utils.JsonUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    ExtensionClient client;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void shouldGetEmptyUserIfUserNotFoundInExtension() {
        when(client.fetch(User.class, "faker")).thenReturn(Optional.empty());

        StepVerifier.create(userService.getUser("faker"))
            .verifyComplete();

        verify(client, times(1)).fetch(eq(User.class), eq("faker"));
    }

    @Test
    void shouldGetUserIfUserFoundInExtension() {
        User fakeUser = new User();
        when(client.fetch(User.class, "faker")).thenReturn(Optional.of(fakeUser));

        StepVerifier.create(userService.getUser("faker"))
            .assertNext(user -> assertEquals(fakeUser, user))
            .verifyComplete();

        verify(client, times(1)).fetch(eq(User.class), eq("faker"));
    }

    @Test
    void shouldUpdatePasswordIfUserFoundInExtension() {
        User fakeUser = new User();
        fakeUser.setSpec(new User.UserSpec());

        when(client.fetch(User.class, "faker")).thenReturn(Optional.of(fakeUser));

        StepVerifier.create(userService.updatePassword("faker", "new-fake-password"))
            .verifyComplete();

        verify(client, times(1)).fetch(eq(User.class), eq("faker"));
        verify(client, times(1)).update(argThat(extension -> {
            var user = (User) extension;
            return "new-fake-password".equals(user.getSpec().getPassword());
        }));
    }

    @Test
    void shouldNotUpdatePasswordIfUserNotFoundInExtension() {
        when(client.fetch(User.class, "faker")).thenReturn(Optional.empty());

        StepVerifier.create(userService.updatePassword("faker", "new-fake-password"))
            .verifyComplete();

        verify(client, times(1)).fetch(eq(User.class), eq("faker"));
        verify(client, times(0)).update(any());
    }

    @Test
    void shouldListRolesIfUserFoundInExtension() {
        User fakeUser = new User();
        Metadata metadata = new Metadata();
        metadata.setName("faker");
        fakeUser.setMetadata(metadata);
        fakeUser.setSpec(new User.UserSpec());

        when(client.list(eq(RoleBinding.class), any(), any())).thenReturn(getRoleBindings());
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

        when(client.fetch(eq(Role.class), eq("test-A"))).thenReturn(Optional.of(roleA));
        when(client.fetch(eq(Role.class), eq("test-B"))).thenReturn(Optional.of(roleB));
        lenient().when(client.fetch(eq(Role.class), eq("ddd"))).thenReturn(Optional.of(roleC));

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
            userService = spy(userService);

            doReturn(
                Mono.just(createUser("fake-password")),
                Mono.just(createUser("new-password")))
                .when(userService)
                .getUser("fake-user");
            when(passwordEncoder.matches("new-password", "fake-password")).thenReturn(false);
            StepVerifier.create(userService.updateWithRawPassword("fake-user", "new-password"))
                .expectNext(createUser("new-password"))
                .verifyComplete();

            verify(passwordEncoder, times(1)).matches("new-password", "fake-password");
            verify(passwordEncoder, times(1)).encode("new-password");
            verify(userService, times(2)).getUser("fake-user");
        }

        @Test
        void shouldUpdatePasswordIfNoPasswordBefore() {
            userService = spy(userService);

            doReturn(
                Mono.just(createUser("")),
                Mono.just(createUser("new-password")))
                .when(userService)
                .getUser("fake-user");
            StepVerifier.create(userService.updateWithRawPassword("fake-user", "new-password"))
                .expectNext(createUser("new-password"))
                .verifyComplete();

            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(passwordEncoder, times(1)).encode("new-password");
            verify(userService, times(2)).getUser("fake-user");
        }

        @Test
        void shouldDoNothingIfPasswordNotChanged() {
            userService = spy(userService);

            doReturn(
                Mono.just(createUser("fake-password")),
                Mono.just(createUser("new-password")))
                .when(userService)
                .getUser("fake-user");
            when(passwordEncoder.matches("fake-password", "fake-password")).thenReturn(true);

            StepVerifier.create(userService.updateWithRawPassword("fake-user", "fake-password"))
                .expectNextCount(0)
                .verifyComplete();

            verify(passwordEncoder, times(1)).matches("fake-password", "fake-password");
            verify(passwordEncoder, never()).encode("fake-password");
            verify(userService, times(1)).getUser("fake-user");
        }

        @Test
        void shouldDoNothingIfUserNotFound() {
            userService = spy(userService);

            doReturn(Mono.empty()).when(userService).getUser("fake-user");
            StepVerifier.create(userService.updateWithRawPassword("fake-user", "new-password"))
                .expectNextCount(0)
                .verifyComplete();

            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userService, times(1)).getUser(anyString());
        }

        User createUser(String password) {
            var user = new User();
            user.setSpec(new User.UserSpec());
            user.getSpec().setPassword(password);
            return user;
        }
    }

}
