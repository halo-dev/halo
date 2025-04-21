package run.halo.app.infra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Processor that periodically checks for unprocessed outbox events and publishes them
 * to Redis Stream for distribution to other instances.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {
    
    private final OutboxEventRepository repository;
    private final OutboxService outboxService;
    private final RedisStreamEventPublisher eventPublisher;
    private final HaloProperties properties;
    
    /**
     * Periodically processes unprocessed outbox events.
     * Uses ShedLock to ensure only one instance runs this at a time in a distributed environment.
     */
    @Scheduled(fixedDelayString = "${halo.distributed.outbox-processor-interval:5000}")
    @SchedulerLock(name = "outboxProcessor", lockAtLeastFor = "4s", lockAtMostFor = "50s")
    public void processOutboxEvents() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        
        log.debug("Starting outbox event processing");
        
        repository.findByProcessedFalseOrderByCreatedAt()
            .take(100) // Process in batches to avoid overwhelming the system
            .flatMap(outboxEvent -> {
                DistributedEvent event = outboxEvent.toDistributedEvent();
                
                // First publish the event to Redis Stream
                eventPublisher.publish(event);
                
                // Then mark it as processed
                return outboxService.markAsProcessed(outboxEvent.getId())
                    .thenReturn(outboxEvent);
            })
            .doOnNext(outboxEvent -> 
                log.debug("Processed outbox event: {}", outboxEvent.getId()))
            .doOnError(error -> 
                log.error("Error processing outbox events", error))
            .count()
            .subscribe(count -> {
                if (count > 0) {
                    log.info("Processed {} outbox events", count);
                }
            });
    }
    
    /**
     * Cleans up old processed events daily to prevent the outbox table from growing indefinitely.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @SchedulerLock(name = "outboxCleaner", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    public void cleanupOldEvents() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        
        // Delete processed events older than 7 days
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
        
        outboxService.cleanupProcessedEvents(cutoff)
            .subscribe(count -> log.info("Cleaned up {} old outbox events", count));
    }
}