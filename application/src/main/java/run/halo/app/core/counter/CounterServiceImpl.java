package run.halo.app.core.counter;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;

/**
 * Counter service implementation.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class CounterServiceImpl implements CounterService {

    private static final String STATS_CACHE_KEY = "stats";

    private static final Duration STATS_CACHE_TTL = Duration.ofSeconds(10);

    private final ReactiveExtensionClient client;

    private final AsyncCache<String, CounterStats> statsCache;

    @Autowired
    public CounterServiceImpl(ReactiveExtensionClient client) {
        this(client, Ticker.systemTicker());
    }

    CounterServiceImpl(ReactiveExtensionClient client, Ticker ticker) {
        this.client = client;
        this.statsCache = Caffeine.newBuilder()
                .expireAfterWrite(STATS_CACHE_TTL)
                .ticker(ticker)
                .buildAsync();
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
                .andQuery(Queries.in("metadata.name", names))
                .build();
        return client.listAll(Counter.class, options, ExtensionUtil.defaultSort());
    }

    @Override
    public Mono<CounterStats> getStats() {
        // Do not cancel the shared load when a subscriber is cancelled.
        return Mono.defer(() -> Mono.fromFuture(
                statsCache.get(STATS_CACHE_KEY, (key, executor) -> loadStats().toFuture()), true));
    }

    private Mono<CounterStats> loadStats() {
        return client.listAll(Counter.class, new ListOptions(), Sort.unsorted())
                .reduce(CounterStats.EMPTY, CounterStats::plus);
    }

    @Override
    public Mono<Counter> deleteByName(String counterName) {
        return client.fetch(Counter.class, counterName).flatMap(client::delete);
    }
}
