package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BackupMalformedException extends ResponseStatusException {

    public BackupMalformedException(String reason, Throwable cause, String detailCode, Object[] detailArgs) {
        super(HttpStatus.BAD_REQUEST, reason, cause, detailCode, detailArgs);
    }
}
