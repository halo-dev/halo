package run.halo.app.core.user.service;

import org.springframework.web.server.ServerWebInputException;

/**
 * Invalid reset token exception.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class InvalidResetTokenException extends ServerWebInputException {

    public InvalidResetTokenException() {
        super("Invalid reset token");
    }

}
