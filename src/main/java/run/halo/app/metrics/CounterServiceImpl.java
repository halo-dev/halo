package run.halo.app.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import org.springframework.stereotype.Service;
import run.halo.app.core.extension.Counter;
import run.halo.app.extension.Metadata;

/**
 * Counter service implementation.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class CounterServiceImpl implements CounterService {

    private final MeterRegistry meterRegistry;

    public CounterServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Counter getByName(String counterName) {
        Tag commonTag = MeterUtils.METRICS_COMMON_TAG;
        Collection<io.micrometer.core.instrument.Counter> counters = meterRegistry.find(counterName)
            .tag(commonTag.getKey(),
                valueMatch -> commonTag.getValue().equals(valueMatch))
            .counters();

        Counter counter = emptyCounter(counterName);
        counter.populateFrom(counters);
        return counter;
    }

    private Counter emptyCounter(String name) {
        Counter counter = new Counter();
        counter.setMetadata(new Metadata());
        counter.getMetadata().setName(name);
        return counter;
    }
}
