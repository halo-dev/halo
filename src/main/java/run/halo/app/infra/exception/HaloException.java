package run.halo.app.infra.exception;

public class HaloException extends RuntimeException {

    public HaloException() {
    }

    public HaloException(String message) {
        super(message);
    }

    public HaloException(String message, Throwable cause) {
        super(message, cause);
    }

    public HaloException(Throwable cause) {
        super(cause);
    }

    public HaloException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
