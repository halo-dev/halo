package run.halo.app.infra.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for managing outbox events, implementing the Transactional Outbox pattern.
 * This ensures that events are reliably delivered even if the Redis messaging system
 * is temporarily unavailable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    
    /**
     * Saves an event to the outbox table, to be processed later.
     * This method is designed to be called within a business transaction.
     *
     * @param event The event to save
     * @return A Mono containing the saved OutboxEvent
     */
    @Transactional
    public Mono<OutboxEvent> saveEvent(DistributedEvent event) {
        String metadataJson = null;
        try {
            if (event.getMetadata() != null) {
                metadataJson = objectMapper.writeValueAsString(event.getMetadata());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event metadata", e);
            return Mono.error(e);
        }
        
        OutboxEvent outboxEvent = OutboxEvent.builder()
            .id(UUID.randomUUID().toString())
            .type(event.getType().name())
            .entityId(event.getEntityId())
            .entityType(event.getEntityType())
            .operation(event.getOperation())
            .metadataJson(metadataJson)
            .createdAt(Instant.now())
            .processed(false)
            .build();
            
        return repository.save(outboxEvent)
            .doOnSuccess(saved -> log.debug("Saved event to outbox: {}", saved.getId()))
            .doOnError(error -> log.error("Failed to save event to outbox", error));
    }
    
    /**
     * Marks an event as processed.
     *
     * @param id The ID of the event to mark as processed
     * @return A Mono that completes when the update is done
     */
    public Mono<Void> markAsProcessed(String id) {
        return repository.markAsProcessed(id, Instant.now())
            .doOnSuccess(v -> log.debug("Marked outbox event {} as processed", id))
            .doOnError(error -> log.error("Failed to mark outbox event {} as processed", id, error));
    }
    
    /**
     * Cleans up old processed events to prevent the outbox table from growing indefinitely.
     *
     * @param olderThan Events processed before this time will be deleted
     * @return A Mono containing the number of deleted records
     */
    public Mono<Integer> cleanupProcessedEvents(Instant olderThan) {
        return repository.deleteProcessedEventsBefore(olderThan)
            .doOnSuccess(count -> {
                if (count > 0) {
                    log.info("Cleaned up {} processed outbox events", count);
                }
            });
    }
}