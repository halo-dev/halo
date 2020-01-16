package run.halo.app.exception;

/**
 * Unsupported media type exception.
 *
 * @author johnniang
 * @date 19-4-19
 */
public class UnsupportedMediaTypeException extends BadRequestException {

    public UnsupportedMediaTypeException(String message) {
        super(message);
    }

    public UnsupportedMediaTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
