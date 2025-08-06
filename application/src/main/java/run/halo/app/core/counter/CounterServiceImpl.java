package run.halo.app.core.counter;

import java.util.Collection;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;

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
    public Flux<Counter> getByNames(Collection<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Flux.empty();
        }
        var options = ListOptions.builder()
            .andQuery(QueryFactory.in("metadata.name", names))
            .build();
        return client.listAll(Counter.class, options, ExtensionUtil.defaultSort());
    }

    @Override
    public Mono<Counter> deleteByName(String counterName) {
        return client.fetch(Counter.class, counterName)
            .flatMap(client::delete);
    }
}
