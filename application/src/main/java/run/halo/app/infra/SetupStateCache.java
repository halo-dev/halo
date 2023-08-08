package run.halo.app.infra;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

/**
 * <p>A cache that caches system setup state.</p>
 * when setUp state changed, the cache will be updated.
 *
 * @author guqing
 * @since 2.5.2
 */
@Component
public class SetupStateCache implements Reconciler<Reconciler.Request>, Supplier<Boolean> {
    private final ExtensionClient client;

    private final InternalValueCache valueCache = new InternalValueCache();

    public SetupStateCache(ExtensionClient client) {
        this.client = client;
    }

    /**
     * <p>Gets system setup state.</p>
     * Never return null.
     *
     * @return <code>true</code> if system is initialized, <code>false</code> otherwise.
     */
    @NonNull
    @Override
    public Boolean get() {
        return valueCache.get();
    }

    @Override
    public Result reconcile(Request request) {
        if (!SystemState.SYSTEM_STATES_CONFIGMAP.equals(request.name())) {
            return Result.doNotRetry();
        }
        valueCache.cache(isInitializedSync());
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new ConfigMap())
            .build();
    }

    /**
     * Check if system is initialized.
     *
     * @return <code>true</code> if system is initialized, <code>false</code> otherwise.
     */
    public boolean isInitializedSync() {
        return client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)
            .map(config -> {
                SystemState systemState = SystemState.deserialize(config);
                return isTrue(systemState.getIsSetup());
            })
            .orElse(false);
    }

    static class InternalValueCache {
        private final AtomicBoolean value = new AtomicBoolean(false);

        public boolean cache(boolean newValue) {
            return value.getAndSet(newValue);
        }

        public boolean get() {
            return value.get();
        }
    }
}
