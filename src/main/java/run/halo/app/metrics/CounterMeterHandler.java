package run.halo.app.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Counter meter handler for {@link Counter}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class CounterMeterHandler implements DisposableBean {

    private final ReactiveExtensionClient client;
    private final MeterRegistry meterRegistry;

    public CounterMeterHandler(ReactiveExtensionClient client, MeterRegistry meterRegistry) {
        this.client = client;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Synchronize counter meters from {@link Counter}.
     *
     * @param event application ready event
     */
    @EventListener(ApplicationReadyEvent.class)
    public Mono<Void> onApplicationReady(ApplicationReadyEvent event) {
        return client.list(Counter.class, null, null)
            .map(counter -> {
                String name = counter.getMetadata().getName();
                // visit counter
                io.micrometer.core.instrument.Counter visitCounter =
                    MeterUtils.visitCounter(meterRegistry, name);
                visitCounter.increment(nullSafe(counter.getVisit()));

                // upvote counter
                io.micrometer.core.instrument.Counter upvoteCounter =
                    MeterUtils.upvoteCounter(meterRegistry, name);
                upvoteCounter.increment(nullSafe(counter.getUpvote()));

                // downvote counter
                io.micrometer.core.instrument.Counter downvoteCounter =
                    MeterUtils.downvoteCounter(meterRegistry, name);
                downvoteCounter.increment(nullSafe(counter.getDownvote()));

                // total comment counter
                io.micrometer.core.instrument.Counter totalCommentCounter =
                    MeterUtils.totalCommentCounter(meterRegistry, name);
                totalCommentCounter.increment(nullSafe(counter.getTotalComment()));

                // approved comment counter
                io.micrometer.core.instrument.Counter approvedCommentCounter =
                    MeterUtils.approvedCommentCounter(meterRegistry, name);
                approvedCommentCounter.increment(nullSafe(counter.getApprovedComment()));
                return counter;
            })
            .then();
    }

    int nullSafe(Integer value) {
        return Objects.requireNonNullElse(value, 0);
    }

    /**
     * Synchronize memory counter meter to the database every minute.
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void counterPersistenceTask() {
        log.debug("Regularly synchronize counter meters to the database.");
        save().block();
    }

    Mono<Void> save() {
        Map<String, List<Meter>> nameMeters = meterRegistry.getMeters().stream()
            .filter(meter -> meter instanceof io.micrometer.core.instrument.Counter)
            .filter(counter -> {
                Meter.Id id = counter.getId();
                return id.getTag(MeterUtils.METRICS_COMMON_TAG.getKey()) != null;
            })
            .collect(Collectors.groupingBy(meter -> meter.getId().getName()));
        Stream<Mono<Counter>> monoStream = nameMeters.entrySet().stream()
            .map(entry -> {
                String name = entry.getKey();
                List<Meter> meters = entry.getValue();
                return client.fetch(Counter.class, name)
                    .switchIfEmpty(Mono.defer(() -> {
                        Counter counter = emptyCounter(name);
                        return client.create(counter);
                    }))
                    .flatMap(counter -> {
                        Counter oldCounter = JsonUtils.deepCopy(counter);
                        counter.populateFrom(meters);
                        if (oldCounter.equals(counter)) {
                            return Mono.empty();
                        }
                        return Mono.just(counter);
                    })
                    .flatMap(client::update);
            });
        return Flux.fromStream(monoStream)
            .flatMap(Function.identity())
            .then();
    }

    static Counter emptyCounter(String name) {
        Counter counter = new Counter();
        counter.setMetadata(new Metadata());
        counter.getMetadata().setName(name);
        counter.setUpvote(0);
        counter.setTotalComment(0);
        counter.setApprovedComment(0);
        counter.setVisit(0);
        return counter;
    }

    @Override
    public void destroy() {
        log.debug("Persist counter meters to database before destroy...");
        save().block();
    }
}
