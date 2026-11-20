package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

class RolePermissionRoleTemplateTest {

    @Test
    void shouldRetireRoleAndPermissionTemplates() {
        var roles = loadRoles();

        assertThat(roles).allSatisfy(role -> {
            assertThat(role.getMetadata().getName()).isNotBlank();
            assertThat(role.getMetadata().getLabels()).containsEntry(Role.HIDDEN_LABEL_NAME, Boolean.TRUE.toString());
            assertThat(role.getMetadata().getAnnotations()).isNullOrEmpty();
            assertThat(role.getRules()).isEmpty();
        });
    }

    @Test
    void shouldRetainAllTombstones() {
        assertThat(loadRoles().stream()
                        .map(role -> role.getMetadata().getName())
                        .toList())
                .containsExactlyInAnyOrder(
                        "role-template-manage-roles",
                        "role-template-view-roles",
                        "role-template-manage-permissions",
                        "role-template-view-permissions");
    }

    private static List<Role> loadRoles() {
        return Stream.of("extensions/role-template-role.yaml", "extensions/role-template-permissions.yaml")
                .flatMap(location -> new YamlUnstructuredLoader(new ClassPathResource(location)).load().stream())
                .map(unstructured -> Unstructured.OBJECT_MAPPER.convertValue(unstructured.getData(), Role.class))
                .toList();
    }
}
