package run.halo.app.extension.exception;

/**
 * ExtensionConvertException is thrown when an Extension conversion error occurs.
 *
 * @author johnniang
 */
public class ExtensionConvertException extends ExtensionException {

    public ExtensionConvertException(String reason) {
        super(reason);
    }

    public ExtensionConvertException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
