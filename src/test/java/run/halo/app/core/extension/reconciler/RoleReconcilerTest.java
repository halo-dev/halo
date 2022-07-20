package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.TestRole;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;

/**
 * Tests for {@link RoleReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class RoleReconcilerTest {

    @Mock
    private ExtensionClient extensionClient;

    @Mock
    private RoleService roleService;

    private RoleReconciler roleReconciler;

    @BeforeEach
    void setUp() {
        roleReconciler = new RoleReconciler(extensionClient, roleService);
    }

    @Test
    void reconcile() throws JSONException {
        Role roleManage = TestRole.getRoleManage();
        Map<String, String> manageAnnotations = new HashMap<>();
        manageAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-view\"]");
        roleManage.getMetadata().setAnnotations(manageAnnotations);

        Role roleView = TestRole.getRoleView();

        Role roleOther = TestRole.getRoleOther();
        when(roleService.listDependencies(eq(Set.of("role-template-apple-view"))))
            .thenReturn(List.of(roleView, roleOther));

        when(extensionClient.fetch(eq(Role.class), eq("role-template-apple-manage"))).thenReturn(
            Optional.of(roleManage));

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        doNothing().when(extensionClient).update(roleCaptor.capture());

        Reconciler.Request request = new Reconciler.Request("role-template-apple-manage");
        roleReconciler.reconcile(request);
        String expected = """
            [
                {
                    "resources": ["apples"],
                    "verbs": ["list"]
                },
                {
                    "resources": ["apples"],
                    "verbs": ["update"]
                }
            ]
            """;
        Role updateArgs = roleCaptor.getValue();
        assertThat(updateArgs).isNotNull();
        JSONAssert.assertEquals(expected, updateArgs.getMetadata().getAnnotations()
            .get(Role.ROLE_DEPENDENCY_RULES), false);
    }

    @Test
    void reconcileUiPermission() {
        Role roleManage = TestRole.getRoleManage();
        Map<String, String> annotations = new LinkedHashMap<>();
        annotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-view\"]");
        annotations.put(Role.UI_PERMISSIONS_ANNO, "[\"apples:manage\"]");
        roleManage.getMetadata().setAnnotations(annotations);

        Role roleView = TestRole.getRoleView();
        Map<String, String> roleViewAnnotations = new LinkedHashMap<>();
        roleViewAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-other\"]");
        roleViewAnnotations.put(Role.UI_PERMISSIONS_ANNO, "[\"apples:view\"]");
        roleView.getMetadata().setAnnotations(roleViewAnnotations);

        Role roleOther = TestRole.getRoleOther();
        Map<String, String> roleOtherAnnotations = new LinkedHashMap<>();
        roleOtherAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-other\"]");
        roleOtherAnnotations.put(Role.UI_PERMISSIONS_ANNO, "[\"apples:foo\", \"apples:bar\"]");
        roleOther.getMetadata().setAnnotations(roleOtherAnnotations);

        when(extensionClient.fetch(eq(Role.class), eq("role-template-apple-manage"))).thenReturn(
            Optional.of(roleManage));

        when(roleService.listDependencies(any()))
            .thenReturn(List.of(roleView, roleOther));

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);

        roleReconciler.reconcile(new Reconciler.Request("role-template-apple-manage"));
        verify(extensionClient, times(1)).update(roleCaptor.capture());

        // assert that the user has the correct roles
        Role value = roleCaptor.getValue();
        Map<String, String> resultAnnotations = value.getMetadata().getAnnotations();
        assertThat(resultAnnotations).isNotNull();
        assertThat(resultAnnotations.containsKey(Role.UI_PERMISSIONS_AGGREGATED_ANNO)).isTrue();
        assertThat(resultAnnotations.get(Role.UI_PERMISSIONS_AGGREGATED_ANNO)).isEqualTo(
            "[\"apples:bar\",\"apples:foo\",\"apples:view\"]");
    }
}