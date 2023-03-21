package run.halo.app.plugin;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>Use this class to collect error information when the plugin enables an error.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@AllArgsConstructor(staticName = "of")
public class PluginStartingError implements Serializable {

    private String pluginId;

    private String message;

    private String devMessage;
}
