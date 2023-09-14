package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NotificationContext}.
 *
 * @author guqing
 * @since 2.9.0
 */
class NotificationContextTest {

    @Test
    void constructTest() {
        // Create a test message payload
        NotificationContext.MessagePayload payload = new NotificationContext.MessagePayload();
        payload.setTitle("Test Title");
        payload.setRawBody("Test Body");
        payload.setHtmlBody("Html body");

        // Create a test subject
        NotificationContext.Subject subject = NotificationContext.Subject.builder()
            .apiVersion("v1")
            .kind("test")
            .name("test-name")
            .title("Test Subject")
            .url("https://example.com")
            .build();

        // Create a test message
        NotificationContext.Message message = new NotificationContext.Message();
        message.setPayload(payload);
        message.setSubject(subject);
        message.setRecipient("test-recipient");
        message.setTimestamp(Instant.now());

        // Create a test receiver config
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode receiverConfig = mapper.createObjectNode();
        receiverConfig.put("key", "value");

        // Create a test sender config
        ObjectNode senderConfig = mapper.createObjectNode();
        senderConfig.put("key", "value");

        // Create a test notification context
        NotificationContext notificationContext = new NotificationContext();
        notificationContext.setMessage(message);
        notificationContext.setReceiverConfig(receiverConfig);
        notificationContext.setSenderConfig(senderConfig);

        // Test getter methods
        assertThat(notificationContext.getMessage()).isNotNull();
        assertThat(notificationContext.getMessage().getPayload()).isEqualTo(payload);
        assertThat(notificationContext.getMessage().getSubject()).isEqualTo(subject);
        assertThat("test-recipient").isEqualTo(notificationContext.getMessage().getRecipient());
        assertThat(notificationContext.getMessage().getTimestamp()).isNotNull();
        assertThat(notificationContext.getReceiverConfig()).isEqualTo(receiverConfig);
        assertThat(notificationContext.getSenderConfig()).isEqualTo(senderConfig);
    }
}