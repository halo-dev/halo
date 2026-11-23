package run.halo.app.infra.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Unstructured;
import run.halo.app.notification.NotificationContext;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests the bridge between the Jackson 3 mapper used by Halo and the Jackson 2 types that are still part of the plugin
 * API, e.g. {@link NotificationContext#getReceiverConfig()}.
 */
class JacksonAdapterModuleTest {

    private final JsonMapper mapper = JsonMapper.builder()
            .addModule(new JacksonAdapterModule(() -> Unstructured.OBJECT_MAPPER))
            .build();

    @Test
    void shouldCodecLegacyJsonNode() {
        var node = Unstructured.OBJECT_MAPPER.createObjectNode().put("name", "fake");

        var json = mapper.writeValueAsString(node);

        assertEquals("{\"name\":\"fake\"}", json);
        assertEquals(node, mapper.readValue(json, ObjectNode.class));
    }

    @Test
    void shouldCodecNotificationContextWithLegacyNodes() {
        var context = new NotificationContext();
        context.setReceiverConfig(Unstructured.OBJECT_MAPPER.createObjectNode().put("email", "fake@example.com"));
        context.setSenderConfig(Unstructured.OBJECT_MAPPER.createObjectNode().put("host", "smtp.example.com"));

        var json = mapper.writeValueAsString(context);
        var deserialized = mapper.readValue(json, NotificationContext.class);

        assertEquals(context, deserialized);
    }
}
