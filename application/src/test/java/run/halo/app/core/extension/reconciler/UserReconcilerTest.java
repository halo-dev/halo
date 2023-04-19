package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.core.extension.User.GROUP;
import static run.halo.app.core.extension.User.KIND;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.ExternalUrlSupplier;

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
    private RoleService roleService;

    @InjectMocks
    private UserReconciler userReconciler;

    @BeforeEach
    void setUp() {
        lenient().when(roleService.listRoleRefs(any())).thenReturn(Flux.empty());
    }

    @Test
    void permalinkForFakeUser() throws URISyntaxException {
        when(externalUrlSupplier.get()).thenReturn(new URI("http://localhost:8090"));

        when(client.fetch(eq(User.class), eq("fake-user")))
            .thenReturn(Optional.of(user("fake-user")));
        userReconciler.reconcile(new Reconciler.Request("fake-user"));
        verify(client, times(2)).update(any(User.class));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(client, times(2)).update(captor.capture());
        assertThat(captor.getValue().getStatus().getPermalink())
            .isEqualTo("http://localhost:8090/authors/fake-user");
    }

    @Test
    void permalinkForAnonymousUser() {
        when(client.fetch(eq(User.class), eq(AnonymousUserConst.PRINCIPAL)))
            .thenReturn(Optional.of(user(AnonymousUserConst.PRINCIPAL)));
        userReconciler.reconcile(new Reconciler.Request(AnonymousUserConst.PRINCIPAL));
        verify(client, times(1)).update(any(User.class));
    }

    @Test
    void ensureRoleNamesAnno() {
        RoleBinding.RoleRef roleRef = new RoleBinding.RoleRef();
        roleRef.setName("fake-role");
        roleRef.setKind(Role.KIND);

        roleRef.setApiGroup(Role.GROUP);
        RoleBinding.RoleRef notworkRef = new RoleBinding.RoleRef();
        notworkRef.setName("super-role");
        notworkRef.setKind("Fake");
        notworkRef.setApiGroup("fake.halo.run");

        RoleBinding.Subject subject = new RoleBinding.Subject(KIND, "fake-user", GROUP);
        when(roleService.listRoleRefs(eq(subject))).thenReturn(Flux.just(roleRef, notworkRef));

        when(client.fetch(eq(User.class), eq("fake-user")))
            .thenReturn(Optional.of(user("fake-user")));

        when(externalUrlSupplier.get()).thenReturn(URI.create("/"));

        userReconciler.reconcile(new Reconciler.Request("fake-user"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(client, times(2)).update(captor.capture());
        User user = captor.getAllValues().get(1);
        assertThat(user.getMetadata().getAnnotations().get(User.ROLE_NAMES_ANNO))
            .isEqualTo("[\"fake-role\"]");
    }

    User user(String name) {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        user.getMetadata().setFinalizers(Set.of("user-protection"));
        return user;
    }
}