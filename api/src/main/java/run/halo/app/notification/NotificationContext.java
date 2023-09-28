package run.halo.app.notification;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
public class NotificationContext {

    private Message message;

    private ObjectNode receiverConfig;

    private ObjectNode senderConfig;

    @Data
    public static class Message {
        private MessagePayload payload;

        private Subject subject;

        private String recipient;

        private Instant timestamp;
    }

    @Data
    @Builder
    public static class Subject {
        private String apiVersion;
        private String kind;
        private String name;
        private String title;
        private String url;
    }

    @Data
    public static class MessagePayload {
        private String title;

        private String rawBody;

        private String htmlBody;

        private ReasonAttributes attributes;
    }
}
