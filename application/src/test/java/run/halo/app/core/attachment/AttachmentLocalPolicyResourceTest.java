package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.FileCategoryMatcher;
import run.halo.app.infra.utils.JsonUtils;
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

    @ParameterizedTest
    @CsvSource({
        "image/png, true",
        "image/jpeg, true",
        "image/webp, true",
        "video/mp4, true",
        "video/webm, true",
        "audio/mpeg, true",
        "audio/ogg, true",
        "image/svg+xml, false",
        "text/html, false",
        "text/javascript, false",
        "text/xml, false",
        "application/pdf, false",
        "application/zip, false",
        "application/octet-stream, false"
    })
    void defaultPolicyShouldOnlyAllowMediaFiles(String mimeType, boolean allowed) {
        var resource = new ClassPathResource("extensions/attachment-local-policy.yaml");
        var configMap = new YamlUnstructuredLoader(resource)
                .load().stream()
                        .filter(unstructured -> "ConfigMap".equals(unstructured.getKind()))
                        .filter(unstructured -> "default-policy-config"
                                .equals(unstructured.getMetadata().getName()))
                        .map(unstructured -> Unstructured.OBJECT_MAPPER.convertValue(unstructured, ConfigMap.class))
                        .findFirst()
                        .orElseThrow();
        var setting = JsonUtils.jsonToObject(configMap.getData().get("default"), JsonNode.class);
        var allowedFileTypes = setting.required("allowedFileTypes");

        assertThat(allowedFileTypes.isArray()).isTrue();
        assertThat(allowedFileTypes.isEmpty()).isFalse();
        assertThat(StreamSupport.stream(allowedFileTypes.spliterator(), false)
                        .map(JsonNode::asText)
                        .map(FileCategoryMatcher::of)
                        .anyMatch(matcher -> matcher.match(mimeType)))
                .isEqualTo(allowed);
    }

    @Test
    void defaultPolicyShouldLimitFileSizeTo200MB() {
        var resource = new ClassPathResource("extensions/attachment-local-policy.yaml");
        var configMap = new YamlUnstructuredLoader(resource)
                .load().stream()
                        .filter(unstructured -> "ConfigMap".equals(unstructured.getKind()))
                        .filter(unstructured -> "default-policy-config"
                                .equals(unstructured.getMetadata().getName()))
                        .map(unstructured -> Unstructured.OBJECT_MAPPER.convertValue(unstructured, ConfigMap.class))
                        .findFirst()
                        .orElseThrow();
        var setting = JsonUtils.jsonToObject(configMap.getData().get("default"), JsonNode.class);

        assertThat(setting.required("maxFileSize").asText()).isEqualTo("200MB");
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
