package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Not found exception.
 *
 * @author guqing
 * @since 2.0.0
 */
public class NotFoundException extends ResponseStatusException {

    public NotFoundException(@Nullable String reason) {
        this(reason, null);
    }

    public NotFoundException(@Nullable String reason,
        @Nullable Throwable cause) {
        super(HttpStatus.NOT_FOUND, reason, cause);
    }

    public NotFoundException(@Nullable Throwable cause) {
        this(cause == null ? "" : cause.getMessage(), cause);
    }
}
