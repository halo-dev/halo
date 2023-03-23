package run.halo.app.metrics;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Counter service implementation.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class CounterServiceImpl implements CounterService {

    private final ReactiveExtensionClient client;

    public CounterServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<Counter> getByName(String counterName) {
        return client.fetch(Counter.class, counterName);
    }

    @Override
    public Mono<Counter> deleteByName(String counterName) {
        return client.fetch(Counter.class, counterName)
            .flatMap(client::delete);
    }
}
