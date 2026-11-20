package run.halo.app.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.method;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.security.authorization.AttributesRecord;
import run.halo.app.security.authorization.RbacRequestEvaluation;
import run.halo.app.security.authorization.RequestInfoFactory;

class MigrationRoleTemplateTest {

    @Test
    void shouldBeRetiredIntoSecureTombstone() {
        var roles = loadRoles();

        assertThat(roles).hasSize(1);
        var role = roles.getFirst();
        assertThat(role.getMetadata().getName()).isEqualTo("role-template-manage-migration");
        assertThat(role.getMetadata().getLabels()).containsEntry(Role.HIDDEN_LABEL_NAME, Boolean.TRUE.toString());
        assertThat(role.getMetadata().getAnnotations()).isNullOrEmpty();
        assertThat(role.getRules()).isEmpty();
    }

    @Test
    void shouldDenyMigrationOperationsWithoutAdditionalRules() {
        var rules =
                loadRoles().stream().flatMap(role -> role.getRules().stream()).toList();
        var requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(
                method(HttpMethod.POST, "/apis/console.api.migration.halo.run/v1alpha1/restorations")
                        .build());

        assertThat(new RbacRequestEvaluation().rulesAllow(new AttributesRecord(requestInfo), rules))
                .isFalse();
    }

    private static List<Role> loadRoles() {
        return new YamlUnstructuredLoader(new ClassPathResource("extensions/role-template-migration.yaml"))
                .load().stream()
                        .map(unstructured ->
                                Unstructured.OBJECT_MAPPER.convertValue(unstructured.getData(), Role.class))
                        .toList();
    }
}
