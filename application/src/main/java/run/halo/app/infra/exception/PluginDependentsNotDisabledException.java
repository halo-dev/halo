package run.halo.app.infra.exception;

import java.net.URI;
import java.util.List;
import org.springframework.web.server.ServerWebInputException;

/**
 * Plugin dependents not disabled exception.
 *
 * @author johnniang
 */
public class PluginDependentsNotDisabledException extends ServerWebInputException {

    public static final URI TYPE =
        URI.create("https://www.halo.run/probs/plugin-dependents-not-disabled");

    /**
     * Instantiates a new Plugin dependents not disabled exception.
     *
     * @param dependents dependents that are not disabled
     */
    public PluginDependentsNotDisabledException(List<String> dependents) {
        super("Plugin dependents are not fully disabled, please disable them first.",
            null,
            null,
            null,
            new Object[] {dependents});
        setType(TYPE);
        getBody().setProperty("dependents", dependents);
    }

}
