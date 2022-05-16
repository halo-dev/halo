package run.halo.app.extension.exception;

/**
 * ExtensionException is the superclass of those exceptions that can be thrown by Extension module.
 *
 * @author johnniang
 */
public class ExtensionException extends RuntimeException {

    public ExtensionException() {
    }

    public ExtensionException(String message) {
        super(message);
    }

    public ExtensionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtensionException(Throwable cause) {
        super(cause);
    }

    public ExtensionException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
