package run.halo.app.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Counter;
import run.halo.app.event.post.VisitedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;

/**
 * Update counters after receiving visit event.
 * It will cache the count in memory for one minute and then batch update to the database.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class VisitedEventReconciler
    implements Reconciler<VisitedEventReconciler.VisitCountBucket>, SmartLifecycle {
    private volatile boolean running = false;

    private final ExtensionClient client;
    private final RequestQueue<VisitCountBucket> visitedEventQueue;
    private final Map<String, Integer> pooledVisitsMap = new ConcurrentHashMap<>();
    private final Controller visitedEventController;

    public VisitedEventReconciler(ExtensionClient client) {
        this.client = client;
        visitedEventQueue = new DefaultQueue<>(Instant::now);
        visitedEventController = this.setupWith(null);
    }

    @Override
    public Result reconcile(VisitCountBucket visitCountBucket) {
        createOrUpdateVisits(visitCountBucket.name(), visitCountBucket.visits());
        return new Result(false, null);
    }

    private void createOrUpdateVisits(String name, Integer visits) {
        client.fetch(Counter.class, name)
            .ifPresentOrElse(counter -> {
                Integer existingVisit = ObjectUtils.defaultIfNull(counter.getVisit(), 0);
                counter.setVisit(existingVisit + visits);
                client.update(counter);
            }, () -> {
                Counter counter = Counter.emptyCounter(name);
                counter.setVisit(visits);
                client.create(counter);
            });
    }

    /**
     * Put the merged data into the queue every minute for updating to the database.
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void queuedVisitBucketTask() {
        Iterator<Map.Entry<String, Integer>> iterator = pooledVisitsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> item = iterator.next();
            visitedEventQueue.addImmediately(new VisitCountBucket(item.getKey(), item.getValue()));
            iterator.remove();
        }
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return new DefaultController<>(
            this.getClass().getName(),
            this,
            visitedEventQueue,
            null,
            Duration.ofMillis(300),
            Duration.ofMinutes(5));
    }

    @EventListener(VisitedEvent.class)
    public void handlePostPublished(VisitedEvent visitedEvent) {
        mergeVisits(visitedEvent);
    }

    @Override
    public void start() {
        this.visitedEventController.start();
        this.running = true;
    }

    @Override
    public void stop() {
        log.debug("Persist visits to database before destroy...");
        try {
            Iterator<Map.Entry<String, Integer>> iterator = pooledVisitsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> item = iterator.next();
                createOrUpdateVisits(item.getKey(), item.getValue());
                iterator.remove();
            }
        } catch (Exception e) {
            log.error("Failed to persist visits to database.", e);
        }
        this.running = false;
        this.visitedEventController.dispose();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    private void mergeVisits(VisitedEvent event) {
        String counterName =
            MeterUtils.nameOf(event.getGroup(), event.getPlural(), event.getName());
        pooledVisitsMap.compute(counterName, (name, visits) -> {
            if (visits == null) {
                return 1;
            } else {
                return visits + 1;
            }
        });
    }

    public record VisitCountBucket(String name, int visits) {
    }
}
