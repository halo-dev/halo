package run.halo.app.core.extension.theme;

import java.io.InputStream;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Theme;

public interface ThemeService {

    Mono<Theme> install(InputStream is);

    Mono<Theme> upgrade(String themeName, InputStream is);

    Mono<Theme> reloadTheme(String name);
    // TODO Migrate other useful methods in ThemeEndpoint in the future.

}
