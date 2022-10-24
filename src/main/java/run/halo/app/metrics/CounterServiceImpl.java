package run.halo.app.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Counter service implementation.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class CounterServiceImpl implements CounterService {

    private final MeterRegistry meterRegistry;
    private final ReactiveExtensionClient client;

    public CounterServiceImpl(MeterRegistry meterRegistry, ReactiveExtensionClient client) {
        this.meterRegistry = meterRegistry;
        this.client = client;
    }

    @Override
    public Counter getByName(String counterName) {
        Collection<io.micrometer.core.instrument.Counter> counters =
            findCounters(counterName);

        Counter counter = emptyCounter(counterName);
        counter.populateFrom(counters);
        return counter;
    }

    private Collection<io.micrometer.core.instrument.Counter> findCounters(String counterName) {
        Tag commonTag = MeterUtils.METRICS_COMMON_TAG;
        return meterRegistry.find(counterName)
            .tag(commonTag.getKey(),
                valueMatch -> commonTag.getValue().equals(valueMatch))
            .counters();
    }

    @Override
    public Mono<Counter> deleteByName(String counterName) {
        return client.fetch(Counter.class, counterName)
            .flatMap(counter -> client.delete(counter)
                .doOnNext(deleted -> findCounters(counterName).forEach(meterRegistry::remove))
                .thenReturn(counter));
    }

    private Counter emptyCounter(String name) {
        Counter counter = new Counter();
        counter.setMetadata(new Metadata());
        counter.getMetadata().setName(name);
        return counter;
    }
}
