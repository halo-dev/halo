package run.halo.app.plugin;

import java.util.Map;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

/**
 * The {@link ReactiveSettingFetcher} to help plugin fetch own setting configuration.
 *
 * @author guqing
 * @since 2.4.0
 */
public interface ReactiveSettingFetcher {

    <T> Mono<T> fetch(String group, Class<T> clazz);

    @NonNull
    Mono<JsonNode> get(String group);

    @NonNull
    Mono<Map<String, JsonNode>> getValues();
}
