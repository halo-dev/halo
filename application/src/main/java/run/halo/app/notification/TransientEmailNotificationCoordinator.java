package run.halo.app.notification;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import run.halo.app.core.extension.notification.Subscription;

/** Coordinates transient email publication and subscription cleanup within one Halo instance. */
@Component
public class TransientEmailNotificationCoordinator {
    private final ConcurrentHashMap<Key, Sinks.One<Void>> operations = new ConcurrentHashMap<>();

    public Mono<Void> publish(String reasonType, String subscriber, Supplier<Mono<Void>> publisher) {
        return Mono.defer(() -> execute(new Key(reasonType, subscriber), publisher, true));
    }

    Mono<Subscription> tryCleanup(String reasonType, String subscriber, Supplier<Mono<Subscription>> cleanup) {
        return Mono.defer(() -> execute(new Key(reasonType, subscriber), cleanup, false));
    }

    private <T> Mono<T> execute(Key key, Supplier<Mono<T>> operation, boolean wait) {
        return Mono.defer(() -> {
                    var completion = Sinks.<Void>one();
                    var current = operations.putIfAbsent(key, completion);
                    if (current != null) {
                        return wait ? current.asMono().then(execute(key, operation, true)) : Mono.<T>empty();
                    }
                    return Mono.defer(operation)
                            // Release before completion reaches subscribers so an immediate
                            // follow-up operation is not skipped; doFinally still covers cancellation.
                            .doOnSuccess(ignored -> release(key, completion))
                            .doOnError(ignored -> release(key, completion))
                            .doFinally(signal -> release(key, completion));
                })
                // A cancelled HTTP request must not release the guard while its writes are still in flight.
                .cache();
    }

    private void release(Key key, Sinks.One<Void> completion) {
        operations.remove(key, completion);
        completion.tryEmitEmpty();
    }

    private record Key(String reasonType, String subscriber) {}
}
