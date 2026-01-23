package run.halo.app.plugin;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.utils.ReactiveUtils;
import tools.jackson.databind.JsonNode;

/**
 * <p>A value fetcher for plugin form configuration.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultSettingFetcher extends SettingFetcher {
    private static final Duration BLOCKING_TIMEOUT = ReactiveUtils.DEFAULT_TIMEOUT;
    private final ReactiveSettingFetcher delegateFetcher;

    public DefaultSettingFetcher(ReactiveSettingFetcher reactiveSettingFetcher) {
        this.delegateFetcher = reactiveSettingFetcher;
    }

    @NonNull
    @Override
    public <T> Optional<T> fetch(String group, Class<T> clazz) {
        return delegateFetcher.fetch(group, clazz)
            .blockOptional(BLOCKING_TIMEOUT);
    }

    @NonNull
    @Override
    public JsonNode get(String group) {
        return Objects.requireNonNull(delegateFetcher.get(group).block(BLOCKING_TIMEOUT));
    }

    /**
     * Get values from {@link ConfigMap}.
     *
     * @return a unmodifiable map of values(non-null).
     */
    @NonNull
    @Override
    public Map<String, JsonNode> getValues() {
        return Objects.requireNonNull(delegateFetcher.getValues().block(BLOCKING_TIMEOUT));
    }
}
