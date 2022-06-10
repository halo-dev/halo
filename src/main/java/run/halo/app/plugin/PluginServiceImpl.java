package run.halo.app.plugin;

import java.util.List;
import org.springframework.stereotype.Service;
import run.halo.app.extension.ExtensionClient;

/**
 * Default implementation of {@link PluginService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class PluginServiceImpl implements PluginService {

    private final ExtensionClient extensionClient;

    public PluginServiceImpl(ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    /**
     * list all plugins including loaded and unloaded.
     *
     * @return plugin info
     */
    public List<Plugin> list() {
        return extensionClient.list(Plugin.class, null, null);
    }
}
