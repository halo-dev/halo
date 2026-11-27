package run.halo.app.core.attachment;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.method;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.Role.PolicyRule;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.security.authorization.AttributesRecord;
import run.halo.app.security.authorization.RbacRequestEvaluation;
import run.halo.app.security.authorization.RequestInfoFactory;

class AttachmentRoleTemplateTest {

    @Test
    void shouldGrantConsoleMatchPermalinksToAttachmentManagers() throws IOException {
        var content = new ClassPathResource("extensions/role-template-attachment.yaml").getContentAsString(UTF_8);

        assertThat(content).contains("console.api.storage.halo.run").contains("attachments/match-permalinks");
    }

    @Test
    void shouldGrantUcMatchPermalinksToAttachmentManagers() throws IOException {
        var content = new ClassPathResource("extensions/role-template-uc-attachment.yaml").getContentAsString(UTF_8);

        assertThat(content).contains("uc.api.storage.halo.run").contains("attachments/match-permalinks");
    }

    @Test
    void shouldEvaluateMatchPermalinksAsUploadPermissionSubresource() {
        var request = method(
                        HttpMethod.POST, "/apis/console.api.storage.halo.run/v1alpha1/attachments/-/match-permalinks")
                .build();
        var requestInfo = RequestInfoFactory.INSTANCE.newRequestInfo(request);
        var attributes = new AttributesRecord(requestInfo);

        assertThat(requestInfo.getVerb()).isEqualTo("create");
        assertThat(requestInfo.getResource()).isEqualTo("attachments");
        assertThat(requestInfo.getSubresource()).isEqualTo("match-permalinks");
        assertThat(new RbacRequestEvaluation()
                        .rulesAllow(
                                attributes,
                                List.of(new PolicyRule.Builder()
                                        .apiGroups("console.api.storage.halo.run")
                                        .resources("attachments/match-permalinks")
                                        .verbs("create")
                                        .build())))
                .isTrue();
        assertThat(new RbacRequestEvaluation()
                        .rulesAllow(
                                attributes,
                                List.of(new PolicyRule.Builder()
                                        .apiGroups("console.api.storage.halo.run")
                                        .resources("attachments")
                                        .verbs("create")
                                        .build())))
                .isFalse();
    }

    @Test
    void shouldKeepStoragePolicySettingsSuperAdminOnly() {
        var rules = loadRules();
        var evaluation = new RbacRequestEvaluation();

        var getPolicy = RequestInfoFactory.INSTANCE.newRequestInfo(
                method(HttpMethod.GET, "/apis/storage.halo.run/v1alpha1/policies/default-policy")
                        .build());
        assertThat(evaluation.rulesAllow(new AttributesRecord(getPolicy), rules))
                .isTrue();

        var updatePolicy = RequestInfoFactory.INSTANCE.newRequestInfo(
                method(HttpMethod.PUT, "/apis/storage.halo.run/v1alpha1/policies/default-policy")
                        .build());
        assertThat(evaluation.rulesAllow(new AttributesRecord(updatePolicy), rules))
                .isFalse();

        var getPolicySetting = RequestInfoFactory.INSTANCE.newRequestInfo(
                method(HttpMethod.GET, "/api/v1alpha1/settings/local-policy-template-setting")
                        .build());
        assertThat(evaluation.rulesAllow(new AttributesRecord(getPolicySetting), rules))
                .isFalse();
    }

    private static List<PolicyRule> loadRules() {
        return new YamlUnstructuredLoader(new ClassPathResource("extensions/role-template-attachment.yaml"))
                .load().stream()
                        .map(unstructured ->
                                Unstructured.OBJECT_MAPPER.convertValue(unstructured.getData(), Role.class))
                        .flatMap(role -> role.getRules().stream())
                        .toList();
    }
}
