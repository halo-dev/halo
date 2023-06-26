package run.halo.app.infra;

import io.micrometer.common.util.StringUtils;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * <p>A cache that caches system setup state.</p>
 * when setUp state changed, the cache will be updated.
 *
 * @author guqing
 * @since 2.5.2
 */
@Component
public class SetupStateCache implements Reconciler<Reconciler.Request>, Supplier<Boolean> {
    public static final String SYSTEM_STATES_CONFIGMAP = "system-states";
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
        if (!SYSTEM_STATES_CONFIGMAP.equals(request.name())) {
            return Result.doNotRetry();
        }
        valueCache.cache(isInitialized());
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
    private boolean isInitialized() {
        return client.fetch(ConfigMap.class, SYSTEM_STATES_CONFIGMAP)
            .filter(configMap -> configMap.getData() != null)
            .map(ConfigMap::getData)
            .flatMap(map -> Optional.ofNullable(map.get(SystemStates.GROUP))
                .filter(StringUtils::isNotBlank)
                .map(value -> JsonUtils.jsonToObject(value, SystemStates.class).getIsSetup())
            )
            .orElse(false);
    }

    @Data
    static class SystemStates {
        static final String GROUP = "states";
        Boolean isSetup;
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
