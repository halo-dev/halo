package run.halo.app.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.Function;
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
import run.halo.app.infra.utils.MeterUtils;

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
                io.micrometer.core.instrument.Counter meterCounter =
                    MeterUtils.counter(meterRegistry, name);
                meterCounter.increment(counter.getCount());
                return meterCounter;
            })
            .then();
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
        Stream<Mono<Counter>> monoStream = meterRegistry.getMeters().stream()
            .filter(meter -> meter instanceof io.micrometer.core.instrument.Counter)
            .filter(counter -> {
                Meter.Id id = counter.getId();
                return id.getTag(MeterUtils.METRICS_COMMON_TAG.getKey()) != null;
            })
            .map(meter -> {
                String name = meter.getId().getName();
                double count = ((io.micrometer.core.instrument.Counter) meter).count();
                return client.fetch(Counter.class, name)
                    .flatMap(counter -> {
                        Integer oldCount = counter.getCount();
                        counter.setCount((int) count);
                        if (oldCount.equals(counter.getCount())) {
                            return Mono.just(counter);
                        }
                        return client.update(counter);
                    }).switchIfEmpty(Mono.defer(() -> {
                        Counter counter = new Counter();
                        counter.setMetadata(new Metadata());
                        counter.getMetadata().setName(name);
                        counter.setCount((int) count);
                        return client.create(counter);
                    }));
            });
        return Flux.fromStream(monoStream)
            .flatMap(Function.identity())
            .collectList()
            .then();
    }

    @Override
    public void destroy() {
        log.debug("Persist counter meters to database before destroy...");
        save().block();
    }
}
