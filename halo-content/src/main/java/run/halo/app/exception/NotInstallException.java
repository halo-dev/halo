package run.halo.app.exception;

/**
 * Not install exception.
 *
 * @author johnniang
 * @date 19-4-29
 */
public class NotInstallException extends BadRequestException {

    public NotInstallException(String message) {
        super(message);
    }

    public NotInstallException(String message, Throwable cause) {
        super(message, cause);
    }
}
