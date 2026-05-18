package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AgreementNotAcceptedException extends ResponseStatusException {

    public AgreementNotAcceptedException() {
        this("Agreement not accepted");
    }

    public AgreementNotAcceptedException(String reason) {
        this(reason, null, null);
    }

    public AgreementNotAcceptedException(String reason, String messageDetailCode, Object[] messageDetailArguments) {
        super(HttpStatus.BAD_REQUEST, reason, null, messageDetailCode, messageDetailArguments);
    }

    public AgreementNotAcceptedException(String reason, Throwable cause) {
        this(reason, cause, null, null);
    }

    public AgreementNotAcceptedException(
            String reason, Throwable cause, String messageDetailCode, Object[] messageDetailArguments) {
        super(HttpStatus.BAD_REQUEST, reason, cause, messageDetailCode, messageDetailArguments);
    }
}
