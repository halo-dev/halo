package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FileTypeNotAllowedException extends ResponseStatusException {

    public FileTypeNotAllowedException(String reason, String messageDetailCode,
        Object[] messageDetailArguments) {
        this(reason, null, messageDetailCode, messageDetailArguments);
    }

    public FileTypeNotAllowedException(String reason, Throwable cause,
        String messageDetailCode, Object[] messageDetailArguments) {
        super(HttpStatus.UNSUPPORTED_MEDIA_TYPE, reason, cause, messageDetailCode,
            messageDetailArguments);
    }
}
