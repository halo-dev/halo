package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

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

    private RoleReconciler roleReconciler;

    private String roleOther;

    @BeforeEach
    void setUp() {
        roleReconciler = new RoleReconciler(extensionClient);
        Role roleManage = JsonUtils.jsonToObject("""
            {
                 "apiVersion": "v1alpha1",
                 "kind": "Role",
                 "metadata": {
                     "name": "role-template-apple-manage",
                     "annotations": {
                         "halo.run/dependencies": "[\\"role-template-apple-view\\"]",
                         "halo.run/module": "Apple Management",
                         "halo.run/display-name": "苹果管理"
                     }
                 },
                 "rules": [{
                     "resources": ["apples"],
                     "verbs": ["create"]
                 }]
             }
            """, Role.class);

        Role roleView = JsonUtils.jsonToObject("""
            {
                 "apiVersion": "v1alpha1",
                 "kind": "Role",
                 "metadata": {
                     "name": "role-template-apple-view",
                     "annotations": {
                         "halo.run/dependencies": "[\\"role-template-apple-other\\"]"
                     }
                 },
                 "rules": [{
                     "resources": ["apples"],
                     "verbs": ["list"]
                 }]
             }
            """, Role.class);

        roleOther = """
            {
                "apiVersion": "v1alpha1",
                "kind": "Role",
                "metadata": {
                    "name": "role-template-apple-other"
                },
                "rules": [{
                    "resources": ["apples"],
                    "verbs": ["update"]
                }]
            }
            """;
        when(extensionClient.fetch(eq(Role.class), eq("role-template-apple-manage"))).thenReturn(
            Optional.of(roleManage));
        when(extensionClient.fetch(eq(Role.class), eq("role-template-apple-view"))).thenReturn(
            Optional.of(roleView));
    }

    @Test
    void reconcile() throws JSONException {
        when(extensionClient.fetch(eq(Role.class), eq("role-template-apple-other"))).thenReturn(
            Optional.of(JsonUtils.jsonToObject(roleOther, Role.class)));
        assertReconcile();
    }

    @Test
    void detectingCycle() throws JSONException {
        Role roleToUse = JsonUtils.jsonToObject(roleOther, Role.class);
        // build a cycle in the dependency
        Map<String, String> annotations =
            Map.of("halo.run/dependencies", "[\"role-template-apple-view\"]");
        roleToUse.getMetadata().setAnnotations(annotations);
        when(extensionClient.fetch(eq(Role.class), eq("role-template-apple-other"))).thenReturn(
            Optional.of(roleToUse));

        assertReconcile();
    }

    private void assertReconcile() throws JSONException {
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
            .get(RoleReconciler.ROLE_DEPENDENCY_RULES), false);
    }
}