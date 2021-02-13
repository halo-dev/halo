package run.halo.app.handler.theme.config;

import java.io.IOException;
import org.springframework.lang.NonNull;
import run.halo.app.handler.theme.config.support.ThemeProperty;

/**
 * Theme file resolver.
 *
 * @author johnniang
 * @date 2019-04-11
 */
public interface ThemePropertyResolver {

    /**
     * Resolves the theme file.
     *
     * @param content file content must not be null
     * @return theme file
     */
    @NonNull
    ThemeProperty resolve(@NonNull String content) throws IOException;
}
