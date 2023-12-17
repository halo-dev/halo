package run.halo.app.infra.exception;

import java.net.URI;
import org.springframework.web.server.ServerWebInputException;

/**
 * PluginAlreadyExistsException indicates the provided plugin has already installed before.
 *
 * @author johnniang
 */
public class PluginAlreadyExistsException extends ServerWebInputException {

    public static final String PLUGIN_ALREADY_EXISTS_TYPE =
        "https://halo.run/probs/plugin-alreay-exists";

    public PluginAlreadyExistsException(String pluginName) {
        super("Plugin already exists.", null, null, null, new Object[] {pluginName});
        setType(URI.create(PLUGIN_ALREADY_EXISTS_TYPE));
        getBody().setProperty("pluginName", pluginName);
    }
}
