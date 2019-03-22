package cc.ryanc.halo.exception;

/**
 * Missing property value exception.
 *
 * @author johnniang
 * @date 3/22/19
 */
public class MissingPropertyValueException extends BadRequestException {

    public MissingPropertyValueException(String message) {
        super(message);
    }

    public MissingPropertyValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
