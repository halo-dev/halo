package run.halo.app.security.sudo;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import run.halo.app.infra.exception.Exceptions;

/**
 * Thrown when a sensitive operation requires sudo confirmation.
 *
 * @author johnniang
 * @since 2.27.0
 */
public class SudoRequiredException extends ResponseStatusException {

    public SudoRequiredException(List<String> methods) {
        super(HttpStatus.FORBIDDEN, "Sudo confirmation required", null, "problemDetail.sudo.required", null);
        setType(URI.create(Exceptions.SUDO_REQUIRED_TYPE));
        getBody().setProperty("methods", methods);
    }
}
