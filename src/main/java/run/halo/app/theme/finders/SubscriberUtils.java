package run.halo.app.theme.finders;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Blocking subscription utility.
 *
 * @author guqing
 * @since 2.0.0
 */
public class SubscriberUtils {

    /**
     * Block the current thread until the Mono completes.
     *
     * @param mono a single value publisher
     * @param <T> the type of the value
     * @return a value
     */
    public static <T> T subscribe(Mono<T> mono) {
        BlockingMonoSubscriber<T> blockingMonoSubscriber = new BlockingMonoSubscriber<>();
        mono.subscribe(blockingMonoSubscriber);
        return blockingMonoSubscriber.blockingGet();
    }

    /**
     * Block the current thread until the Flux completes.
     *
     * @param flux the multivalued publisher
     * @param <T> the type of the value
     * @return a list
     */
    public static <T> List<T> subscribe(Flux<T> flux) {
        BlockingMonoSubscriber<List<T>> blockingMonoSubscriber = new BlockingMonoSubscriber<>();
        flux.collectList()
            .subscribe(blockingMonoSubscriber);
        return blockingMonoSubscriber.blockingGet();
    }
}
