package run.halo.app.infra.exception;

/**
 * Not found exception.
 *
 * @author guqing
 * @since 2.0.0
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }
}
