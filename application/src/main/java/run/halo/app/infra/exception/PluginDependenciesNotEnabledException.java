package run.halo.app.infra.exception;

import java.net.URI;
import java.util.List;
import org.springframework.web.server.ServerWebInputException;

/**
 * Plugin dependencies not enabled exception.
 *
 * @author johnniang
 */
public class PluginDependenciesNotEnabledException extends ServerWebInputException {

    public static final URI TYPE =
        URI.create("https://www.halo.run/probs/plugin-dependencies-not-enabled");

    /**
     * Instantiates a new Plugin dependencies not enabled exception.
     *
     * @param dependencies dependencies that are not enabled
     */
    public PluginDependenciesNotEnabledException(List<String> dependencies) {
        super("Plugin dependencies are not fully enabled, please enable them first.",
            null,
            null,
            null,
            new Object[] {dependencies});
        setType(TYPE);
        getBody().setProperty("dependencies", dependencies);
    }

}
