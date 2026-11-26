package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.method;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.security.authorization.AttributesRecord;
import run.halo.app.security.authorization.RbacRequestEvaluation;
import run.halo.app.security.authorization.RequestInfoFactory;

class ConfigMapRoleTemplateTest {

    @Test
    void shouldRetireConfigMapTemplates() {
        assertThat(loadRoles()).allSatisfy(role -> {
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
                .containsExactlyInAnyOrder("role-template-manage-configmaps", "role-template-view-configmaps");
    }

    @Test
    void shouldDenyConfigMapAccessWithoutAdditionalRules() {
        var rules =
                loadRoles().stream().flatMap(role -> role.getRules().stream()).toList();
        var evaluation = new RbacRequestEvaluation();

        var update = RequestInfoFactory.INSTANCE.newRequestInfo(
                method(HttpMethod.PUT, "/api/v1alpha1/configmaps/system").build());
        assertThat(evaluation.rulesAllow(new AttributesRecord(update), rules)).isFalse();

        var get = RequestInfoFactory.INSTANCE.newRequestInfo(
                method(HttpMethod.GET, "/api/v1alpha1/configmaps/system").build());
        assertThat(evaluation.rulesAllow(new AttributesRecord(get), rules)).isFalse();
    }

    private static List<Role> loadRoles() {
        return new YamlUnstructuredLoader(new ClassPathResource("extensions/role-template-configmap.yaml"))
                .load().stream()
                        .map(unstructured ->
                                Unstructured.OBJECT_MAPPER.convertValue(unstructured.getData(), Role.class))
                        .toList();
    }
}
