package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FileSizeExceededException extends ResponseStatusException {

    public FileSizeExceededException(String reason, String messageDetailCode,
        Object[] messageDetailArguments) {
        this(reason, null, messageDetailCode, messageDetailArguments);
    }

    public FileSizeExceededException(String reason, Throwable cause,
        String messageDetailCode, Object[] messageDetailArguments) {
        super(HttpStatus.PAYLOAD_TOO_LARGE, reason, cause, messageDetailCode,
            messageDetailArguments);
    }
}
