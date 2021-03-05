package run.halo.app.exception;

/**
 * Theme not found exception.
 *
 * @author johnniang
 * @date 2020-03-01
 */
public class ThemeNotFoundException extends BadRequestException {

    public ThemeNotFoundException(String message) {
        super(message);
    }

    public ThemeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
