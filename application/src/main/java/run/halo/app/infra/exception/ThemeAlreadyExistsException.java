package run.halo.app.infra.exception;

import java.net.URI;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebInputException;

/**
 * {@link ThemeAlreadyExistsException} indicates the provided theme has already installed before.
 *
 * @author guqing
 * @since 2.6.0
 */
public class ThemeAlreadyExistsException extends ServerWebInputException {

    public static final String THEME_ALREADY_EXISTS_TYPE =
        "https://halo.run/probs/theme-alreay-exists";

    /**
     * Constructs a {@code ThemeAlreadyExistsException} with the given theme name.
     *
     * @param themeName theme name must not be blank
     */
    public ThemeAlreadyExistsException(@NonNull String themeName) {
        super("Theme already exists.", null, null, "problemDetail.theme.install.alreadyExists",
            new Object[] {themeName});
        setType(URI.create(THEME_ALREADY_EXISTS_TYPE));
        getBody().setProperty("themeName", themeName);
    }
}
