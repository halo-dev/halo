package run.halo.app.core.endpoint.uc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.method;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import run.halo.app.core.extension.Role.PolicyRule;
import run.halo.app.security.authorization.AttributesRecord;
import run.halo.app.security.authorization.RbacRequestEvaluation;
import run.halo.app.security.authorization.RequestInfoFactory;

class UcUserRoleTemplateTest {

    @Test
    void shouldGrantUcUserInfoToAuthenticatedUsers() throws IOException {
        var content = new ClassPathResource("extensions/role-template-authenticated.yaml").getContentAsString(UTF_8);

        assertThat(content)
                .contains("role-template-own-user-info")
                .contains("role-template-change-own-password")
                .contains("uc.api.halo.run")
                .contains("users/password");
    }

    @Test
    void shouldEvaluateCurrentUserRequestsAsExpected() {
        var getRequest =
                method(HttpMethod.GET, "/apis/uc.api.halo.run/v1alpha1/users/-").build();
        var getRequestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(getRequest);
        var getAttributes = new AttributesRecord(getRequestInfo);

        assertThat(getRequestInfo.getVerb()).isEqualTo("get");
        assertThat(getRequestInfo.getResource()).isEqualTo("users");
        assertThat(getRequestInfo.getSubresource()).isEqualTo("");
        assertThat(getRequestInfo.getName()).isEqualTo("-");
        assertThat(new RbacRequestEvaluation()
                        .rulesAllow(
                                getAttributes,
                                List.of(new PolicyRule.Builder()
                                        .apiGroups("uc.api.halo.run")
                                        .resources("users")
                                        .resourceNames("-")
                                        .verbs("get")
                                        .build())))
                .isTrue();

        var putRequest = method(HttpMethod.PUT, "/apis/uc.api.halo.run/v1alpha1/users/-/password")
                .build();
        var putRequestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(putRequest);
        var putAttributes = new AttributesRecord(putRequestInfo);

        assertThat(putRequestInfo.getVerb()).isEqualTo("update");
        assertThat(putRequestInfo.getResource()).isEqualTo("users");
        assertThat(putRequestInfo.getSubresource()).isEqualTo("password");
        assertThat(putRequestInfo.getName()).isEqualTo("-");
        assertThat(new RbacRequestEvaluation()
                        .rulesAllow(
                                putAttributes,
                                List.of(new PolicyRule.Builder()
                                        .apiGroups("uc.api.halo.run")
                                        .resources("users/password")
                                        .resourceNames("-")
                                        .verbs("update")
                                        .build())))
                .isTrue();
        // the password subresource must not be allowed by a bare users update rule
        assertThat(new RbacRequestEvaluation()
                        .rulesAllow(
                                putAttributes,
                                List.of(new PolicyRule.Builder()
                                        .apiGroups("uc.api.halo.run")
                                        .resources("users")
                                        .resourceNames("-")
                                        .verbs("update")
                                        .build())))
                .isFalse();
    }
}
