package run.halo.app.extension.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

/**
 * ExtensionException is the superclass of those exceptions that can be thrown by Extension module.
 *
 * @author johnniang
 */
public class ExtensionException extends ResponseStatusException {

    public ExtensionException(String reason) {
        this(reason, null);
    }

    public ExtensionException(String reason, Throwable cause) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause, null, new Object[] {reason});
    }

    protected ExtensionException(HttpStatusCode status, String reason, Throwable cause,
        String messageDetailCode, Object[] messageDetailArguments) {
        super(status, reason, cause, messageDetailCode, messageDetailArguments);
    }
}
