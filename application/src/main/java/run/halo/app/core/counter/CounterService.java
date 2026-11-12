package run.halo.app.core.counter;

import java.util.Collection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface CounterService {

    Mono<Counter> getByName(String counterName);

    Flux<Counter> getByNames(Collection<String> names);

    /**
     * Gets aggregated statistics of all counters.
     *
     * <p>The result may be cached for a short period to avoid scanning all counters on every request.
     *
     * @return aggregated counter statistics
     */
    Mono<CounterStats> getStats();

    Mono<Counter> deleteByName(String counterName);
}
