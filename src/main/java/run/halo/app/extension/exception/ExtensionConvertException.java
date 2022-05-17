package run.halo.app.extension.exception;

/**
 * ExtensionConvertException is thrown when an Extension conversion error occurs.
 *
 * @author johnniang
 */
public class ExtensionConvertException extends ExtensionException {

    public ExtensionConvertException() {
    }

    public ExtensionConvertException(String message) {
        super(message);
    }

    public ExtensionConvertException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtensionConvertException(Throwable cause) {
        super(cause);
    }

    public ExtensionConvertException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
