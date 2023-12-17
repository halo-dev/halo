package run.halo.app.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import run.halo.app.extension.ConfigMap;

/**
 * <p>A value fetcher for plugin form configuration.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultSettingFetcher extends SettingFetcher {
    private final ReactiveSettingFetcher delegateFetcher;

    public DefaultSettingFetcher(ReactiveSettingFetcher reactiveSettingFetcher) {
        this.delegateFetcher = reactiveSettingFetcher;
    }

    @NonNull
    @Override
    public <T> Optional<T> fetch(String group, Class<T> clazz) {
        return delegateFetcher.fetch(group, clazz)
            .blockOptional();
    }

    @NonNull
    @Override
    public JsonNode get(String group) {
        return Objects.requireNonNull(delegateFetcher.get(group).block());
    }

    /**
     * Get values from {@link ConfigMap}.
     *
     * @return a unmodifiable map of values(non-null).
     */
    @NonNull
    @Override
    public Map<String, JsonNode> getValues() {
        return Objects.requireNonNull(delegateFetcher.getValues().block());
    }
}
