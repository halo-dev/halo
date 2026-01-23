package run.halo.app.infra.exception;

import org.springframework.web.server.ServerWebInputException;

/**
 * Restricted name exception.
 *
 * @author lywq
 * @since 2025/10/30 11:47
 **/
public class RestrictedNameException extends ServerWebInputException {

    public RestrictedNameException() {
        super("The name is restricted");
    }
}
