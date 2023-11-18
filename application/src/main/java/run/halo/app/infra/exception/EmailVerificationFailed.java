package run.halo.app.infra.exception;

import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebInputException;

/**
 * Exception thrown when email verification failed.
 *
 * @author guqing
 * @since 2.11.0
 */
public class EmailVerificationFailed extends ServerWebInputException {

    public EmailVerificationFailed() {
        super("Invalid verification code");
    }

    public EmailVerificationFailed(String reason, @Nullable Throwable cause) {
        super(reason, null, cause);
    }

    public EmailVerificationFailed(String reason, @Nullable Throwable cause,
        @Nullable String messageDetailCode, @Nullable Object[] messageDetailArguments) {
        super(reason, null, cause, messageDetailCode, messageDetailArguments);
    }
}
