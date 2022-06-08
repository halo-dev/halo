package run.halo.app.extension.exception;

public class ExtensionNotFoundException extends ExtensionException {

    public ExtensionNotFoundException() {
    }

    public ExtensionNotFoundException(String message) {
        super(message);
    }

    public ExtensionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtensionNotFoundException(Throwable cause) {
        super(cause);
    }

    public ExtensionNotFoundException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
