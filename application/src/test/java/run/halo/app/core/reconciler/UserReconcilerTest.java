package run.halo.app.core.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
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
import run.halo.app.core.extension.User;
import run.halo.app.core.reconciler.UserReconciler;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.notification.NotificationCenter;

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

    @InjectMocks
    private UserReconciler userReconciler;

    @BeforeEach
    void setUp() {
        lenient().when(notificationCenter.unsubscribe(any(), any())).thenReturn(Mono.empty());
    }

    @Test
    void permalinkForFakeUser() throws URISyntaxException {
        when(externalUrlSupplier.get()).thenReturn(new URI("http://localhost:8090"));

        when(roleService.getRolesByUsername("fake-user"))
            .thenReturn(Flux.empty());

        when(client.fetch(eq(User.class), eq("fake-user")))
            .thenReturn(Optional.of(user("fake-user")));
        userReconciler.reconcile(new Reconciler.Request("fake-user"));

        verify(client).<User>update(assertArg(user ->
            assertEquals(
                "http://localhost:8090/authors/fake-user",
                user.getStatus().getPermalink()
            )
        ));
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
        when(client.fetch(eq(User.class), eq("fake-user")))
            .thenReturn(Optional.of(user("fake-user")));
        when(externalUrlSupplier.get()).thenReturn(URI.create("/"));

        userReconciler.reconcile(new Reconciler.Request("fake-user"));

        verify(client).update(assertArg(user -> {
            assertEquals("""
                    ["fake-role"]\
                    """,
                user.getMetadata().getAnnotations().get(User.ROLE_NAMES_ANNO));
        }));
    }

    User user(String name) {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        user.getMetadata().setFinalizers(Set.of("user-protection"));
        user.setSpec(new User.UserSpec());
        return user;
    }
}