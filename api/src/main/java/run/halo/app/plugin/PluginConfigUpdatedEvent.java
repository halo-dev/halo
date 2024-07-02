package run.halo.app.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ConfigMap;

/**
 * <p>Event that is triggered when the {@link ConfigMap } represented by
 * {@link Plugin.PluginSpec#getConfigMapName()} in the {@link Plugin} is updated.</p>
 * <p>has two properties, oldConfig and newConfig, which represent the {@link ConfigMap#getData()}
 * property value of the {@link ConfigMap}.</p>
 *
 * @author guqing
 * @since 2.17.0
 */
@Getter
public class PluginConfigUpdatedEvent extends ApplicationEvent {
    private final Map<String, JsonNode> oldConfig;
    private final Map<String, JsonNode> newConfig;

    @Builder
    public PluginConfigUpdatedEvent(Object source, Map<String, JsonNode> oldConfig,
        Map<String, JsonNode> newConfig) {
        super(source);
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
    }
}
