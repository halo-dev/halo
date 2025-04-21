package run.halo.app.infra.messaging;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Repository for accessing and modifying OutboxEvent entities in the database.
 */
public interface OutboxEventRepository extends ReactiveCrudRepository<OutboxEvent, String> {
    
    /**
     * Find all unprocessed events, ordered by creation time.
     *
     * @return A flux of unprocessed outbox events
     */
    Flux<OutboxEvent> findByProcessedFalseOrderByCreatedAt();
    
    /**
     * Mark an event as processed.
     *
     * @param id The ID of the event to mark as processed
     * @param processedAt The timestamp when the event was processed
     * @return A Mono that completes when the update is done
     */
    @Modifying
    @Query("UPDATE outbox_events SET processed = true, processed_at = :processedAt WHERE id = :id")
    Mono<Void> markAsProcessed(String id, Instant processedAt);
    
    /**
     * Delete processed events older than a specified time.
     *
     * @param olderThan The cutoff timestamp; events processed before this time will be deleted
     * @return A Mono containing the number of deleted records
     */
    @Modifying
    @Query("DELETE FROM outbox_events WHERE processed = true AND processed_at < :olderThan")
    Mono<Integer> deleteProcessedEventsBefore(Instant olderThan);
}