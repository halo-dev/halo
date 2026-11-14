package run.halo.app.core.endpoint.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.method;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.security.authorization.AttributesRecord;
import run.halo.app.security.authorization.RbacRequestEvaluation;
import run.halo.app.security.authorization.RequestInfoFactory;

class UserRoleTemplateTest {
    @ParameterizedTest
    @CsvSource({
        "GET, /api/v1alpha1/users/target, false",
        "POST, /api/v1alpha1/users, false",
        "PUT, /api/v1alpha1/users/target, false",
        "PATCH, /api/v1alpha1/users/target, false",
        "DELETE, /api/v1alpha1/users/target, false",
        "POST, /apis/api.console.halo.run/v1alpha1/users, true",
        "GET, /apis/api.console.halo.run/v1alpha1/users/target, true",
        "PUT, /apis/api.console.halo.run/v1alpha1/users/target, true",
        "DELETE, /apis/api.console.halo.run/v1alpha1/users/target, false",
        "DELETE, /apis/api.console.halo.run/v1alpha1/users, false",
        "DELETE, /api/v1alpha1/users, false",
        "DELETE, /apis/api.console.halo.run/v1alpha1/users/target/avatar, true",
        "POST, /apis/api.console.halo.run/v1alpha1/users/target/permissions, false",
        "PUT, /apis/api.console.halo.run/v1alpha1/users/target/password, false"
    })
    void delegatesOnlyConsoleUserOperations(HttpMethod method, String path, boolean allowed) {
        var roles = new YamlUnstructuredLoader(new ClassPathResource("extensions/role-template-user.yaml"))
                .load().stream()
                        .map(value -> Unstructured.OBJECT_MAPPER.convertValue(value.getData(), Role.class))
                        .filter(role -> List.of("role-template-manage-users", "role-template-view-users")
                                .contains(role.getMetadata().getName()))
                        .toList();
        var manager = roles.stream()
                .filter(role -> role.getMetadata().getName().equals("role-template-manage-users"))
                .findFirst()
                .orElseThrow();
        assertThat(manager.getMetadata().getAnnotations().get(Role.ROLE_DEPENDENCIES_ANNO))
                .doesNotContain("role-template-change-password");
        var rules = roles.stream().flatMap(role -> role.getRules().stream()).toList();
        var requestInfo =
                RequestInfoFactory.INSTANCE.newRequestInfo(method(method, path).build());
        assertThat(new RbacRequestEvaluation().rulesAllow(new AttributesRecord(requestInfo), rules))
                .isEqualTo(allowed);
    }
}
