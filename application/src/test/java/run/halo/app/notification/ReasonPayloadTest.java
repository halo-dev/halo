package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.notification.Reason;

/**
 * Tests for {@link ReasonPayload}.
 *
 * @author guqing
 * @since 2.9.0
 */
class ReasonPayloadTest {

    @Test
    public void testReasonPayloadBuilder() {
        Reason.Subject subject = Reason.Subject.builder()
            .kind("Post")
            .apiVersion("content.halo.run/v1alpha1")
            .name("fake-post")
            .title("Fake post title")
            .url("https://halo.run/fake-post")
            .build();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", 2);
        attributes.put("key3", "value3");

        ReasonPayload reasonPayload = ReasonPayload.builder()
            .subject(subject)
            .attribute("key1", "value1")
            .attribute("key2", 2)
            .attributes(Map.of("key3", "value3"))
            .build();

        assertNotNull(reasonPayload);
        assertThat(reasonPayload).isNotNull();
        assertThat(reasonPayload.getSubject()).isEqualTo(subject);
        assertThat(reasonPayload.getAttributes()).isEqualTo(attributes);
    }
}