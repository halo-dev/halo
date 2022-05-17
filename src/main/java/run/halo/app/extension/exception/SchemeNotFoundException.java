package run.halo.app.extension.exception;

/**
 * SchemeNotFoundException is thrown while we try to get a scheme but not found.
 *
 * @author johnniang
 */
public class SchemeNotFoundException extends ExtensionException {

    public SchemeNotFoundException() {
    }

    public SchemeNotFoundException(String message) {
        super(message);
    }

    public SchemeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemeNotFoundException(Throwable cause) {
        super(cause);
    }

    public SchemeNotFoundException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
