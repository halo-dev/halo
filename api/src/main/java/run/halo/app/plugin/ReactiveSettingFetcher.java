package run.halo.app.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

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
