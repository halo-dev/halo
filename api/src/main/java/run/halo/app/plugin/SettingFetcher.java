package run.halo.app.plugin;

import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/**
 * SettingFetcher must be a class instead of an interface due to backward compatibility.
 *
 * @author johnniang
 */
public abstract class SettingFetcher {

    public abstract <T> Optional<T> fetch(String group, Class<T> clazz);

    public abstract JsonNode get(String group);

    public abstract Map<String, JsonNode> getValues();

}
