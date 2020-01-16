package run.halo.app.exception;

/**
 * Missing property value exception.
 *
 * @author johnniang
 * @date 3/22/19
 */
public class MissingPropertyException extends BadRequestException {

    public MissingPropertyException(String message) {
        super(message);
    }

    public MissingPropertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
