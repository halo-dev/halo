package run.halo.app.theme;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static run.halo.app.utils.FileUtils.findPath;

/**
 * Theme root folder locator.
 *
 * @author johnniang
 */
@Slf4j
public class ThemeRootLocator {

    private final Path path;

    /**
     * Theme property filenames.
     */
    private static final String[] THEME_PROPERTY_FILENAMES = new String[] {
            "theme.yaml",
            "theme.yml",
    };

    public ThemeRootLocator(Path path) {
        this.path = path;
    }

    /**
     * Locate theme root folder.
     *
     * @return theme root folder or empty
     */
    @NonNull
    public Optional<Path> locate() {
        try {
            return findPath(this.path, p -> equalsAnyIgnoreCase(p.getFileName().toString(), THEME_PROPERTY_FILENAMES));
        } catch (IOException ignored) {
            log.warn("Error occurred while finding theme root path", ignored);
        }
        return Optional.empty();
    }

}
