package run.halo.app.infra;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static run.halo.app.extension.index.query.Queries.isNull;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

/**
 * A cache that caches system setup state. when setUp state changed, the cache will be updated.
 *
 * <p>The setup state is stored in the {@link SystemState} which is managed by the setup flow. Legacy installations
 * without an explicit state are identified by their existing users.
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
                .mapNotNull(configMap -> SystemState.deserialize(configMap).getIsSetup())
                .map(initialized -> {
                    var setupRequired = !isTrue(initialized);
                    if (!setupRequired) {
                        // A completed setup never becomes incomplete again, so it is safe to cache.
                        setupCompleted.set(true);
                    }
                    return setupRequired;
                })
                .switchIfEmpty(Mono.defer(this::setupRequiredWithoutState));
    }

    private Mono<Boolean> setupRequiredWithoutState() {
        var listOptions = new ListOptions();
        listOptions.setLabelSelector(
                LabelSelector.builder().notEq(User.HIDDEN_USER_LABEL, "true").build());
        listOptions.setFieldSelector(FieldSelector.of(isNull("metadata.deletionTimestamp")));
        return client.listBy(User.class, listOptions, PageRequestImpl.ofSize(1)).map(result -> result.getTotal() == 0);
    }
}
