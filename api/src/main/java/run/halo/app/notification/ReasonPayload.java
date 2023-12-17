package run.halo.app.notification;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import run.halo.app.core.extension.notification.Reason;

/**
 * A value object to hold reason payload.
 *
 * @author guqing
 * @see Reason
 * @since 2.10.0
 */
@Data
@AllArgsConstructor
public class ReasonPayload {
    private Reason.Subject subject;
    private final UserIdentity author;
    private Map<String, Object> attributes;

    public static ReasonPayloadBuilder builder() {
        return new ReasonPayloadBuilder();
    }

    public static class ReasonPayloadBuilder {
        private Reason.Subject subject;
        private UserIdentity author;
        private final Map<String, Object> attributes;

        ReasonPayloadBuilder() {
            this.attributes = new HashMap<>();
        }

        public ReasonPayloadBuilder subject(Reason.Subject subject) {
            this.subject = subject;
            return this;
        }

        public ReasonPayloadBuilder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public ReasonPayloadBuilder attributes(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        public ReasonPayloadBuilder author(UserIdentity author) {
            this.author = author;
            return this;
        }

        public ReasonPayload build() {
            return new ReasonPayload(subject, author, attributes);
        }
    }
}