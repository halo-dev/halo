package run.halo.app.infra.exception;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeUninstallException extends RuntimeException {

    public ThemeUninstallException(String message) {
        super(message);
    }

    public ThemeUninstallException(String message, Throwable cause) {
        super(message, cause);
    }
}
