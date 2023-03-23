package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DuplicateNameException extends ResponseStatusException {

    public DuplicateNameException() {
        this("Duplicate name detected");
    }

    public DuplicateNameException(String reason) {
        this(reason, null);
    }

    public DuplicateNameException(String reason, Throwable cause) {
        this(reason, cause, null, null);
    }

    public DuplicateNameException(String reason, Throwable cause, String messageDetailCode,
        Object[] messageDetailArguments) {
        super(HttpStatus.BAD_REQUEST, reason, cause, messageDetailCode, messageDetailArguments);
    }
}
