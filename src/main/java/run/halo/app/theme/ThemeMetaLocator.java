package run.halo.app.theme;

import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static run.halo.app.utils.FileUtils.findPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

/**
 * Theme meta data locator.
 *
 * @author johnniang
 */
@Slf4j
public enum ThemeMetaLocator {

    INSTANCE;

    /**
     * Theme property filenames.
     */
    private static final String[] THEME_PROPERTY_FILENAMES = new String[] {
        "theme.yaml",
        "theme.yml",
    };

    private static final String[] THEME_SETTING_FILENAMES = new String[] {
        "settings.yaml",
        "settings.yml",
    };

    /**
     * Theme screenshots name.
     */
    private static final String THEME_SCREENSHOTS_NAME = "screenshot";

    /**
     * Locate theme root folder.
     *
     * @param path the given path must not be null
     * @return root path or empty
     */
    @NonNull
    public Optional<Path> locateThemeRoot(@NonNull Path path) {
        return locateProperty(path).map(Path::getParent);
    }

    /**
     * Locate theme property path.
     *
     * @return theme property path or empty
     */
    @NonNull
    public Optional<Path> locateProperty(@NonNull Path path) {
        try {
            var predicate = ((Predicate<Path>)
                Files::isRegularFile)
                .and(Files::isReadable)
                .and(
                    p -> equalsAnyIgnoreCase(p.getFileName().toString(), THEME_PROPERTY_FILENAMES));

            log.debug("Locating property in path: {}", path);
            return findPath(path, 3, predicate);
        } catch (IOException e) {
            log.warn("Error occurred while finding theme root path", e);
        }
        return Optional.empty();
    }

    /**
     * Locate theme setting path.
     *
     * @return theme setting path or empty
     */
    @NonNull
    public Optional<Path> locateSetting(@NonNull Path path) {
        return locateThemeRoot(path).flatMap(root -> {
                try {
                    var predicate = ((Predicate<Path>)
                        Files::isRegularFile)
                        .and(Files::isReadable)
                        .and(p -> equalsAnyIgnoreCase(p.getFileName().toString(),
                            THEME_SETTING_FILENAMES));
                    log.debug("Locating setting from {}", path);
                    return findPath(path, 3, predicate);
                } catch (IOException e) {
                    log.warn("Error occurred while finding theme root path", e);
                }
                return Optional.empty();
            }
        );
    }

    /**
     * Locate screenshot.
     *
     * @param path root path
     * @return screenshot path or empty
     */
    @NonNull
    public Optional<Path> locateScreenshot(@NonNull Path path) {
        return locateThemeRoot(path).flatMap(root -> {
            try (var pathStream = Files.list(root)) {
                var predicate = ((Predicate<Path>) Files::isRegularFile)
                    .and(Files::isReadable)
                    .and(p -> p.getFileName().toString().startsWith(THEME_SCREENSHOTS_NAME));
                log.debug("Locating screenshot from path: {}", path);
                return pathStream.filter(predicate).findFirst();
            } catch (IOException e) {
                log.warn("Failed to list path: " + path, e);
            }
            return Optional.empty();
        });
    }
}
