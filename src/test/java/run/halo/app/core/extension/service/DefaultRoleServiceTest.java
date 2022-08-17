package run.halo.app.core.extension.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.TestRole;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link DefaultRoleService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultRoleServiceTest {
    @Mock
    private ReactiveExtensionClient extensionClient;

    private DefaultRoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new DefaultRoleService(extensionClient);
    }

    @Test
    void listDependencies() {
        Role roleManage = TestRole.getRoleManage();
        Map<String, String> manageAnnotations = new HashMap<>();
        manageAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-view\"]");
        roleManage.getMetadata().setAnnotations(manageAnnotations);

        Role roleView = TestRole.getRoleView();
        Map<String, String> viewAnnotations = new HashMap<>();
        viewAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-other\"]");
        roleView.getMetadata().setAnnotations(viewAnnotations);

        Role roleOther = TestRole.getRoleOther();

        when(extensionClient.fetch(same(Role.class), eq("role-template-apple-manage")))
            .thenReturn(Mono.just(roleManage));
        when(extensionClient.fetch(same(Role.class), eq("role-template-apple-view")))
            .thenReturn(Mono.just(roleView));
        when(extensionClient.fetch(same(Role.class), eq("role-template-apple-other")))
            .thenReturn(Mono.just(roleOther));

        // list without cycle
        List<Role> roles = roleService.listDependencies(Set.of("role-template-apple-manage"));

        verify(extensionClient, times(1)).fetch(same(Role.class), eq("role-template-apple-manage"));
        verify(extensionClient, times(1)).fetch(same(Role.class), eq("role-template-apple-view"));
        verify(extensionClient, times(1)).fetch(same(Role.class), eq("role-template-apple-other"));

        assertThat(roles).hasSize(3);
        assertThat(roles).containsExactly(roleManage, roleView, roleOther);

        // list dependencies with a cycle relation
        Map<String, String> anotherAnnotations = new HashMap<>();
        anotherAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-view\"]");
        roleOther.getMetadata().setAnnotations(anotherAnnotations);
        when(extensionClient.fetch(same(Role.class), eq("role-template-apple-other")))
            .thenReturn(Mono.just(roleOther));
        // correct behavior is to ignore the cycle relation
        List<Role> rolesFromCycle =
            roleService.listDependencies(Set.of("role-template-apple-manage"));
        assertThat(rolesFromCycle).hasSize(3);
    }
}