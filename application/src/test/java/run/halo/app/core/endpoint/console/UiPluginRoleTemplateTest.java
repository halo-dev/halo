package run.halo.app.core.endpoint.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.security.authorization.AttributesRecord;
import run.halo.app.security.authorization.RbacRequestEvaluation;
import run.halo.app.security.authorization.RequestInfoFactory;

class UiPluginRoleTemplateTest {

    @Test
    void shouldAllowAuthenticatedUsersToFetchUiPluginProviders() {
        var resource = new ClassPathResource("extensions/role-template-authenticated.yaml");
        var authenticated = new YamlUnstructuredLoader(resource).load().getFirst();
        var metadata = Objects.requireNonNull(authenticated.getMetadata());
        assertThat(metadata.getName()).isEqualTo("authenticated");
        var role = Unstructured.OBJECT_MAPPER.convertValue(authenticated.getData(), Role.class);

        var request = get("/apis/api.console.halo.run/v1alpha1/ui-plugins/-/providers")
                .build();
        var requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);

        assertThat(requestInfo.getVerb()).isEqualTo("get");
        assertThat(requestInfo.getApiGroup()).isEqualTo("api.console.halo.run");
        assertThat(requestInfo.getResource()).isEqualTo("ui-plugins");
        assertThat(requestInfo.getName()).isEqualTo("-");
        assertThat(requestInfo.getSubresource()).isEqualTo("providers");
        assertThat(new RbacRequestEvaluation().rulesAllow(new AttributesRecord(requestInfo), role.getRules()))
                .isTrue();
    }
}
