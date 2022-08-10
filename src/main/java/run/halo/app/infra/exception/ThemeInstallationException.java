package run.halo.app.infra.exception;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeInstallationException extends RuntimeException {
    public ThemeInstallationException(String message) {
        super(message);
    }

    public ThemeInstallationException(String message, Throwable cause) {
        super(message, cause);
    }
}
