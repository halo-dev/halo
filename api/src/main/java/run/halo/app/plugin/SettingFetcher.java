package run.halo.app.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Optional;

public interface SettingFetcher {

    <T> Optional<T> fetch(String group, Class<T> clazz);

    JsonNode get(String group);

    Map<String, JsonNode> getValues();

}
