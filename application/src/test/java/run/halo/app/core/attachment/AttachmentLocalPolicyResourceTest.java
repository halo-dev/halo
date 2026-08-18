package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * Tests for the built-in local attachment policy resource.
 *
 * <p>The default policy (and its config map) are initialized from <code>extensions/attachment-local-policy.yaml</code>
 * on every startup. The <code>halo.run/do-not-overwrite</code> label is required to prevent the initializer from
 * reverting user modifications, such as the display priority label, on restart.
 *
 * @author bedhere
 * @since 2.27.0
 */
class AttachmentLocalPolicyResourceTest {

    @Test
    void defaultPolicyShouldBeMarkedAsDoNotOverwrite() {
        var resource = new ClassPathResource("extensions/attachment-local-policy.yaml");
        var unstructuredList = new YamlUnstructuredLoader(resource).load();

        var defaultPolicy = unstructuredList.stream()
                .filter(unstructured -> "Policy".equals(unstructured.getKind()))
                .filter(unstructured ->
                        "default-policy".equals(unstructured.getMetadata().getName()))
                .findFirst()
                .orElseThrow();

        assertThat(defaultPolicy.getMetadata().getLabels())
                .as("Default policy must not be overwritten on startup, otherwise user "
                        + "modifications (e.g. upload display priority) are reverted on restart.")
                .containsEntry(ExtensionUtil.DO_NOT_OVERWRITE_LABEL, "true");
    }

    @Test
    void defaultPolicyConfigMapShouldBeMarkedAsDoNotOverwrite() {
        var resource = new ClassPathResource("extensions/attachment-local-policy.yaml");
        var unstructuredList = new YamlUnstructuredLoader(resource).load();

        var configMap = unstructuredList.stream()
                .filter(unstructured -> "ConfigMap".equals(unstructured.getKind()))
                .filter(unstructured -> "default-policy-config"
                        .equals(unstructured.getMetadata().getName()))
                .findFirst()
                .orElseThrow();

        assertThat(configMap.getMetadata().getLabels()).containsEntry(ExtensionUtil.DO_NOT_OVERWRITE_LABEL, "true");
    }
}
