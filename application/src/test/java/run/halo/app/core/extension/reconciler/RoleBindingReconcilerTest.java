package run.halo.app.core.extension.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.core.extension.User.ROLE_NAMES_ANNO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Result;

@ExtendWith(MockitoExtension.class)
class RoleBindingReconcilerTest {

    @Mock
    ExtensionClient client;

    @InjectMocks
    RoleBindingReconciler reconciler;

    final Result doNotReEnQueue = new Result(false, null);

    @Test
    void shouldDoNothingIfRequestNotFound() {
        var bindingName = "fake-binding-name";
        when(client.fetch(RoleBinding.class, bindingName))
            .thenReturn(Optional.empty());

        var result = reconciler.reconcile(new Reconciler.Request(bindingName));
        assertEquals(doNotReEnQueue, result);

        verify(client, times(1)).fetch(same(RoleBinding.class), anyString());
        verify(client, never()).list(same(RoleBinding.class), any(), any());
        verify(client, never()).fetch(same(User.class), anyString());
        verify(client, never()).update(isA(User.class));
    }

    @Test
    void shouldDoNothingIfNotContainAnyUserSubject() {
        var bindingName = "fake-binding-name";
        var binding = mock(RoleBinding.class);

        when(client.fetch(RoleBinding.class, bindingName))
            .thenReturn(Optional.of(binding));
        when(binding.getSubjects()).thenReturn(List.of());

        var result = reconciler.reconcile(new Reconciler.Request(bindingName));
        assertEquals(doNotReEnQueue, result);

        verify(client, times(1)).fetch(same(RoleBinding.class), anyString());
        verify(client, never()).list(same(RoleBinding.class), any(), any());
        verify(client, never()).fetch(same(User.class), anyString());
        verify(client, never()).update(isA(User.class));
    }

    @Test
    void shouldDoNothingIfUserNotFound() {
        var bindingName = "fake-binding-name";
        var userName = "fake-user-name";
        var binding = mock(RoleBinding.class);
        var subject = mock(Subject.class);

        when(client.fetch(RoleBinding.class, bindingName))
            .thenReturn(Optional.of(binding));
        when(binding.getSubjects()).thenReturn(List.of(subject));
        when(subject.getKind()).thenReturn("User");
        when(subject.getName()).thenReturn(userName);
        when(client.fetch(User.class, userName)).thenReturn(Optional.empty());

        var result = reconciler.reconcile(new Reconciler.Request(bindingName));
        assertEquals(doNotReEnQueue, result);

        verify(client, times(1)).fetch(same(RoleBinding.class), anyString());
        verify(client, times(1)).list(same(RoleBinding.class), any(), any());
        verify(client, times(1)).fetch(same(User.class), eq(userName));

        verify(client, never()).update(isA(User.class));
    }

    @Test
    void shouldUpdateRoleNamesIfNoBindingRelatedTheUser() {
        var bindingName = "fake-binding-name";
        var userName = "fake-user-name";
        var subject = mock(Subject.class);
        var binding = createRoleBinding("Role", "fake-role", false, subject);
        var user = mock(User.class);
        var userMetadata = mock(Metadata.class);

        when(client.fetch(RoleBinding.class, bindingName))
            .thenReturn(Optional.of(binding));
        when(binding.getSubjects()).thenReturn(List.of(subject));
        when(subject.getKind()).thenReturn("User");
        when(subject.getName()).thenReturn(userName);
        when(client.fetch(User.class, userName)).thenReturn(Optional.of(user));
        when(user.getMetadata()).thenReturn(userMetadata);
        when(client.list(same(RoleBinding.class), any(), any())).thenReturn(List.of());

        var result = reconciler.reconcile(new Reconciler.Request(bindingName));
        assertEquals(doNotReEnQueue, result);

        verify(client, times(1)).fetch(same(RoleBinding.class), anyString());
        verify(client, times(1)).list(same(RoleBinding.class), any(), any());
        verify(client, times(1)).fetch(same(User.class), anyString());
        verify(client, times(1)).update(isA(User.class));

        verify(userMetadata).setAnnotations(argThat(annotation -> {
            String roleNames = annotation.get(ROLE_NAMES_ANNO);
            return roleNames != null && roleNames.equals("[]");
        }));
    }

    @Test
    void shouldUpdateRoleNames() {
        var bindingName = "fake-binding-name";
        var userName = "fake-user-name";
        var subject = mock(Subject.class);
        when(subject.getKind()).thenReturn("User");
        when(subject.getName()).thenReturn(userName);
        when(subject.getApiGroup()).thenReturn("");

        var user = mock(User.class);
        var userMetadata = mock(Metadata.class);
        var binding = createRoleBinding("Role", "fake-role", false, subject);

        when(client.fetch(RoleBinding.class, bindingName))
            .thenReturn(Optional.of(binding));
        when(binding.getSubjects()).thenReturn(List.of(subject));

        when(client.fetch(User.class, userName)).thenReturn(Optional.of(user));
        when(user.getMetadata()).thenReturn(userMetadata);
        var bindings = List.of(
            createRoleBinding("Role", "fake-role-01", false, subject),
            createRoleBinding("Role", "fake-role-03", false, subject),
            createRoleBinding("Role", "fake-role-02", false, subject),
            createRoleBinding("NotRole", "fake-role-04", false, subject),
            createRoleBinding("Role", "fake-role-05", true, subject)
        );
        when(client.list(same(RoleBinding.class), any(), any())).thenReturn(bindings);

        var result = reconciler.reconcile(new Reconciler.Request(bindingName));
        assertEquals(doNotReEnQueue, result);

        verify(client, times(1)).fetch(same(RoleBinding.class), anyString());
        verify(client, times(1)).list(same(RoleBinding.class), any(), any());
        verify(client, times(1)).fetch(same(User.class), anyString());
        verify(client, times(1)).update(isA(User.class));

        verify(userMetadata).setAnnotations(argThat(annotation -> {
            var roleNames = annotation.get(ROLE_NAMES_ANNO);
            return roleNames != null && roleNames.equals("""
                ["fake-role-01","fake-role-02","fake-role-03"]""");
        }));
    }

    RoleBinding createRoleBinding(String roleRefKind,
        String roleRefName,
        boolean deleting,
        Subject subject) {
        var binding = mock(RoleBinding.class);
        var roleRef = mock(RoleRef.class);
        var metadata = mock(Metadata.class);
        lenient().when(roleRef.getKind()).thenReturn(roleRefKind);
        lenient().when(roleRef.getName()).thenReturn(roleRefName);
        lenient().when(roleRef.getApiGroup()).thenReturn("");
        lenient().when(metadata.getDeletionTimestamp()).thenReturn(deleting ? Instant.now() : null);
        lenient().when(binding.getRoleRef()).thenReturn(roleRef);
        lenient().when(binding.getMetadata()).thenReturn(metadata);
        lenient().when(binding.getSubjects()).thenReturn(List.of(subject));
        return binding;
    }

}
