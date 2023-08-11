package run.halo.app.infra;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * <p>A cache that caches system setup state.</p>
 * when setUp state changed, the cache will be updated.
 *
 * @author guqing
 * @since 2.5.2
 */
@Component
@RequiredArgsConstructor
public class DefaultInitializationStateGetter implements InitializationStateGetter {
    private final ReactiveExtensionClient client;
    private final AtomicBoolean userInitialized = new AtomicBoolean(false);
    private final AtomicBoolean dataInitialized = new AtomicBoolean(false);

    @Override
    public Mono<Boolean> userInitialized() {
        // If user is initialized, return true directly.
        if (userInitialized.get()) {
            return Mono.just(true);
        }
        return hasUser()
            .doOnNext(userInitialized::set);
    }

    @Override
    public Mono<Boolean> dataInitialized() {
        if (dataInitialized.get()) {
            return Mono.just(true);
        }
        return client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)
            .map(config -> {
                SystemState systemState = SystemState.deserialize(config);
                return isTrue(systemState.getIsSetup());
            })
            .defaultIfEmpty(false)
            .doOnNext(dataInitialized::set);
    }

    private Mono<Boolean> hasUser() {
        return client.list(User.class,
                user -> {
                    var labels = MetadataUtil.nullSafeLabels(user);
                    return isNotTrue(labels.get("halo.run/hidden-user"));
                }, null, 1, 10)
            .map(result -> result.getTotal() > 0)
            .defaultIfEmpty(false);
    }

    static boolean isNotTrue(String value) {
        return !Boolean.parseBoolean(value);
    }
}
