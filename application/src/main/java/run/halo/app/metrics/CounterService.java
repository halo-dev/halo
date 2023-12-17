package run.halo.app.metrics;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface CounterService {

    Mono<Counter> getByName(String counterName);

    Mono<Counter> deleteByName(String counterName);
}
