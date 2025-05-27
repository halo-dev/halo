package run.halo.app.infra.exception;

import java.net.URI;
import org.springframework.web.server.ServerErrorException;

/**
 * Exception thrown when an incompatible plugin is detected.
 * This usually occurs when a plugin is not compatible with the current version of Halo at runtime.
 *
 * @author johnniang
 * @since 2.21.0
 */
public class PluginRuntimeIncompatibleException extends ServerErrorException {

    private static final URI TYPE =
        URI.create("https://www.halo.run/probs/plugin-runtime-incompatible");

    public PluginRuntimeIncompatibleException(Throwable cause) {
        super("Incompatible plugin detected.", cause);
        setType(TYPE);
    }

}
