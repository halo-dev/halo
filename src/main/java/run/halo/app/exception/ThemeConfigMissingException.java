package run.halo.app.exception;

/**
 * Theme configuration missing exception.
 *
 * @author johnniang
 * @date 4/11/19
 */
public class ThemeConfigMissingException extends BadRequestException {

    public ThemeConfigMissingException(String message) {
        super(message);
    }

    public ThemeConfigMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
