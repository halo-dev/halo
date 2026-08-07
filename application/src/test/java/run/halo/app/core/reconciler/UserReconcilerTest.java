package run.halo.app.core.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Device;
import run.halo.app.core.extension.RememberMeToken;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequeueException;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.security.PersonalAccessToken;
import run.halo.app.security.device.DeviceService;

/**
 * Tests for {@link UserReconciler}.
 *
 * @author guqing
 * @since 2.0.1
 */
@ExtendWith(MockitoExtension.class)
class UserReconcilerTest {
    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @Mock
    private ExtensionClient client;

    @Mock
    private NotificationCenter notificationCenter;

    @Mock
    private RoleService roleService;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private UserReconciler userReconciler;

    @BeforeEach
    void setUp() {
        lenient().when(notificationCenter.unsubscribe(any(), any())).thenReturn(Mono.empty());
    }

    @Test
    void permalinkForFakeUser() throws URISyntaxException {
        when(externalUrlSupplier.get()).thenReturn(new URI("http://localhost:8090"));

        when(roleService.getRolesByUsername("fake-user")).thenReturn(Flux.empty());

        when(client.fetch(eq(User.class), eq("fake-user"))).thenReturn(Optional.of(user("fake-user")));
        userReconciler.reconcile(new Reconciler.Request("fake-user"));

        verify(client)
                .<User>update(assertArg(user -> assertEquals(
                        "http://localhost:8090/authors/fake-user",
                        user.getStatus().getPermalink())));
    }

    @Test
    void permalinkForAnonymousUser() {
        when(client.fetch(eq(User.class), eq(AnonymousUserConst.PRINCIPAL)))
                .thenReturn(Optional.of(user(AnonymousUserConst.PRINCIPAL)));
        when(roleService.getRolesByUsername(AnonymousUserConst.PRINCIPAL)).thenReturn(Flux.empty());
        userReconciler.reconcile(new Reconciler.Request(AnonymousUserConst.PRINCIPAL));
        verify(client).update(any(User.class));
    }

    @Test
    void ensureRoleNamesAnno() {
        when(roleService.getRolesByUsername("fake-user")).thenReturn(Flux.just("fake-role"));
        when(client.fetch(eq(User.class), eq("fake-user"))).thenReturn(Optional.of(user("fake-user")));
        when(externalUrlSupplier.get()).thenReturn(URI.create("/"));

        userReconciler.reconcile(new Reconciler.Request("fake-user"));

        verify(client).update(assertArg(user -> {
            assertEquals("""
                    ["fake-role"]\
                    """, user.getMetadata().getAnnotations().get(User.ROLE_NAMES_ANNO));
        }));
    }

    @Test
    void shouldAddUserProtectionFinalizerBeforeCreatingUser() {
        var user = user("fake-user");
        user.getMetadata().setFinalizers(null);

        StepVerifier.create(userReconciler.preCreating(user)).verifyComplete();

        assertEquals(Set.of("user-protection"), user.getMetadata().getFinalizers());
    }

    @Test
    void shouldDeleteSoleUserRoleBindingBeforeRemovingUserFinalizer() {
        var user = deletingUser("fake-user");
        var binding = RoleBinding.create("fake-user", "guest");
        when(client.fetch(User.class, "fake-user")).thenReturn(Optional.of(user));
        when(roleService.listRoleBindings(assertArg(subject -> assertEquals("fake-user", subject.getName()))))
                .thenReturn(Flux.just(binding));

        assertThrows(RequeueException.class, () -> userReconciler.reconcile(new Reconciler.Request("fake-user")));

        verify(client).delete(binding);
        verify(client, never()).update(user);
        assertEquals(Set.of("user-protection"), user.getMetadata().getFinalizers());
    }

    @Test
    void shouldDeleteUserAuthenticationDataBeforeRemovingUserFinalizer() {
        var user = deletingUser("fake-user");
        var device = new Device();
        device.setMetadata(new Metadata());
        device.getMetadata().setName("fake-device");
        device.setSpec(new Device.Spec().setPrincipalName("fake-user").setSessionId("fake-session"));
        var rememberMeToken = new RememberMeToken();
        rememberMeToken.setMetadata(new Metadata());
        rememberMeToken.getMetadata().setName("fake-remember-me-token");
        rememberMeToken.setSpec(
                new RememberMeToken.Spec().setUsername("fake-user").setSeries("fake-series"));
        var personalAccessToken = new PersonalAccessToken();
        personalAccessToken.setMetadata(new Metadata());
        personalAccessToken.getMetadata().setName("fake-personal-access-token");
        personalAccessToken.getSpec().setUsername("fake-user");
        when(client.fetch(User.class, "fake-user")).thenReturn(Optional.of(user));
        when(client.listAll(eq(RememberMeToken.class), any(), any())).thenReturn(List.of(rememberMeToken));
        when(client.listAll(eq(Device.class), any(), any())).thenReturn(List.of(device));
        when(client.listAll(eq(PersonalAccessToken.class), any(), any())).thenReturn(List.of(personalAccessToken));
        when(client.listAll(eq(UserConnection.class), any(), any())).thenReturn(List.of());
        when(roleService.listRoleBindings(any())).thenReturn(Flux.empty());
        when(deviceService.revoke("fake-user", "fake-device")).thenReturn(Mono.empty());

        assertThrows(RequeueException.class, () -> userReconciler.reconcile(new Reconciler.Request("fake-user")));

        verify(client).delete(rememberMeToken);
        verify(deviceService).revoke("fake-user", "fake-device");
        verify(client).delete(personalAccessToken);
        verify(client, never()).update(user);
        assertEquals(Set.of("user-protection"), user.getMetadata().getFinalizers());
    }

    @Test
    void shouldRemoveDeletedUserFromSharedRoleBindingBeforeRemovingUserFinalizer() {
        var user = deletingUser("fake-user");
        var binding = RoleBinding.create("fake-user", "guest");
        binding.getSubjects().add(new RoleBinding.Subject(User.KIND, "another-user", User.GROUP));
        when(client.fetch(User.class, "fake-user")).thenReturn(Optional.of(user));
        when(roleService.listRoleBindings(any())).thenReturn(Flux.just(binding));

        assertThrows(RequeueException.class, () -> userReconciler.reconcile(new Reconciler.Request("fake-user")));

        verify(client).update(binding);
        verify(client, never()).delete(binding);
        verify(client, never()).update(user);
        assertFalse(binding.getSubjects().stream().anyMatch(RoleBinding.Subject.isUser("fake-user")));
        assertEquals(List.of(new RoleBinding.Subject(User.KIND, "another-user", User.GROUP)), binding.getSubjects());
    }

    @Test
    void shouldRemoveUserFinalizerAfterConnectionsAndRoleBindingsAreGone() {
        var user = deletingUser("fake-user");
        when(client.fetch(User.class, "fake-user")).thenReturn(Optional.of(user));
        when(roleService.listRoleBindings(any())).thenReturn(Flux.empty());

        userReconciler.reconcile(new Reconciler.Request("fake-user"));

        verify(client).update(user);
        assertFalse(user.getMetadata().getFinalizers().contains("user-protection"));
    }

    User user(String name) {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        user.getMetadata().setFinalizers(Set.of("user-protection"));
        user.setSpec(new User.UserSpec());
        return user;
    }

    User deletingUser(String name) {
        var user = user(name);
        user.getMetadata().setDeletionTimestamp(Instant.parse("2026-08-05T00:00:00Z"));
        return user;
    }
}
