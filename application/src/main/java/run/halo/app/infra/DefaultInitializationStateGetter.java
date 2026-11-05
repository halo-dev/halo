package run.halo.app.infra;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * A cache that caches system setup state. when setUp state changed, the cache will be updated.
 *
 * <p>The setup state is stored in the {@link SystemState} which is managed by the setup flow. An absent state means the
 * system has never been set up.
 *
 * @author guqing
 * @since 2.5.2
 */
@Component
@RequiredArgsConstructor
public class DefaultInitializationStateGetter implements InitializationStateGetter {
    private final ReactiveExtensionClient client;
    private final AtomicBoolean setupCompleted = new AtomicBoolean(false);

    @Override
    public Mono<Boolean> setupRequired() {
        if (setupCompleted.get()) {
            return Mono.just(false);
        }
        return client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)
                .map(configMap -> {
                    var setupRequired =
                            !isTrue(SystemState.deserialize(configMap).getIsSetup());
                    if (!setupRequired) {
                        // A completed setup never becomes incomplete again, so it is safe to cache.
                        setupCompleted.set(true);
                    }
                    return setupRequired;
                })
                // An absent system states ConfigMap means the system has never been set up.
                .defaultIfEmpty(true);
    }
}
