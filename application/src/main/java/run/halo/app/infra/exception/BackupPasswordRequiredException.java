package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Thrown when restoring an encrypted backup without providing a password. */
public class BackupPasswordRequiredException extends ResponseStatusException {

    public BackupPasswordRequiredException() {
        super(
                HttpStatus.BAD_REQUEST,
                "The backup file is encrypted. Please provide the password.",
                null,
                "problemDetail.migration.backup.passwordRequired",
                null);
    }
}
