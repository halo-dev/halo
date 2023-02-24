package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import run.halo.app.core.extension.User;
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
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @Mock
    private ExtensionClient client;

    @InjectMocks
    private UserReconciler userReconciler;

    @Test
    void permalinkForFakeUser() throws URISyntaxException {
        when(externalUrlSupplier.get()).thenReturn(new URI("http://localhost:8090"));

        when(client.fetch(eq(User.class), eq("fake-user")))
            .thenReturn(Optional.of(user("fake-user")));
        userReconciler.reconcile(new Reconciler.Request("fake-user"));
        verify(client, times(1)).update(any(User.class));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(client, times(1)).update(captor.capture());
        assertThat(captor.getValue().getStatus().getPermalink())
            .isEqualTo("http://localhost:8090/authors/fake-user");
    }

    @Test
    void permalinkForAnonymousUser() {
        when(client.fetch(eq(User.class), eq(AnonymousUserConst.PRINCIPAL)))
            .thenReturn(Optional.of(user(AnonymousUserConst.PRINCIPAL)));
        userReconciler.reconcile(new Reconciler.Request(AnonymousUserConst.PRINCIPAL));
        verify(client, times(0)).update(any(User.class));
    }

    User user(String name) {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        user.getMetadata().setFinalizers(Set.of("user-protection"));
        return user;
    }
}