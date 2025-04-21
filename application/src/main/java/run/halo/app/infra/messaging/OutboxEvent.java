package run.halo.app.infra.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an event stored in the outbox table for reliable messaging.
 * This implements the Transactional Outbox pattern for ensuring event delivery.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("outbox_events")
public class OutboxEvent {
    @Id
    private String id;
    private String type;
    private String entityId;
    private String entityType;
    private String operation;
    private String metadataJson;
    private Instant createdAt;
    private boolean processed;
    private Instant processedAt;
    
    /**
     * Converts this outbox event to a DistributedEvent.
     *
     * @return The corresponding DistributedEvent
     */
    public DistributedEvent toDistributedEvent() {
        Map<String, String> metadata = null;
        if (metadataJson != null && !metadataJson.isBlank()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                metadata = objectMapper.readValue(metadataJson, 
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));
            } catch (JsonProcessingException e) {
                // Ignore parsing errors and continue with null metadata
            }
        }
        
        return DistributedEvent.builder()
            .type(DistributedEvent.EventType.valueOf(type))
            .entityId(entityId)
            .entityType(entityType)
            .operation(operation)
            .metadata(metadata)
            .build();
    }
}