package run.halo.app.exception;

/**
 * Property format exception.
 *
 * @author johnniang
 * @date 3/27/19
 */
public class PropertyFormatException extends BadRequestException {

    public PropertyFormatException(String message) {
        super(message);
    }

    public PropertyFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
