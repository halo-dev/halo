package run.halo.app.infra.messaging;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an event that needs to be distributed across multiple Halo instances.
 * Used for cache synchronization and cross-instance notifications.
 */
@Data
@Builder
public class DistributedEvent {
    public enum EventType {
        POST_CREATED,
        POST_UPDATED,
        POST_DELETED,
        COMMENT_CREATED,
        COMMENT_UPDATED,
        COMMENT_DELETED,
        USER_UPDATED,
        TAG_UPDATED,
        CATEGORY_UPDATED
    }

    private EventType type;
    private String entityId;
    private String entityType;
    private String operation;
    private Map<String, String> metadata;

    /**
     * Converts this event to a Map that can be published to a Redis Stream.
     *
     * @return A map representation of this event
     */
    public Map<String, String> toStreamRecord() {
        Map<String, String> record = new HashMap<>();
        record.put("type", type.name());
        record.put("id", entityId);
        record.put("entity", entityType);
        record.put("operation", operation);
        
        if (metadata != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                record.put("meta_" + entry.getKey(), entry.getValue());
            }
        }
        
        return record;
    }
}