package run.halo.app.theme;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.handler.theme.config.ThemePropertyResolver;
import run.halo.app.handler.theme.config.impl.YamlThemePropertyResolver;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.utils.FilenameUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static run.halo.app.service.ThemeService.SETTINGS_NAMES;

/**
 * Theme property scanner.
 *
 * @author johnniang
 */
@Slf4j
public enum ThemePropertyScanner {

    INSTANCE;

    /**
     * Theme property file name.
     */
    private static final String[] THEME_PROPERTY_FILE_NAMES = {"theme.yaml", "theme.yml"};
    /**
     * Theme screenshots name.
     */
    private static final String THEME_SCREENSHOTS_NAME = "screenshot";
    private final ThemePropertyResolver propertyResolver = new YamlThemePropertyResolver();

    /**
     * Scan theme properties.
     *
     * @param themePath them path must not be null
     * @return a list of them property
     */
    @NonNull
    public List<ThemeProperty> scan(@NonNull Path themePath, @Nullable String activeThemeId) {
        // create if absent
        try {
            if (Files.notExists(themePath)) {
                Files.createDirectories(themePath);
            }
        } catch (IOException e) {
            log.error("Failed to create directory: " + themePath, e);
            return Collections.emptyList();
        }
        try (Stream<Path> pathStream = Files.list(themePath)) {
            // List and filter sub folders
            List<Path> themePaths = pathStream.filter(Files::isDirectory)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(themePaths)) {
                return Collections.emptyList();
            }

            // Get theme properties
            ThemeProperty[] properties = themePaths.stream()
                    .map(this::fetchThemeProperty)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .peek(themeProperty -> {
                        if (StringUtils.equals(activeThemeId, themeProperty.getId())) {
                            themeProperty.setActivated(true);
                        }
                    })
                    .toArray(ThemeProperty[]::new);
            // Cache the themes
            return Arrays.asList(properties);
        } catch (IOException e) {
            log.error("Failed to get themes", e);
            return Collections.emptyList();
        }
    }

    /**
     * Fetch theme property
     *
     * @param themePath theme path must not be null
     * @return an optional theme property
     */
    @NonNull
    public Optional<ThemeProperty> fetchThemeProperty(@NonNull Path themePath) {
        Assert.notNull(themePath, "Theme path must not be null");

        Optional<Path> optionalPath = fetchPropertyPath(themePath);

        if (!optionalPath.isPresent()) {
            return Optional.empty();
        }

        Path propertyPath = optionalPath.get();

        try {
            // Get property content
            String propertyContent = new String(Files.readAllBytes(propertyPath), StandardCharsets.UTF_8);

            // Resolve the base properties
            ThemeProperty themeProperty = propertyResolver.resolve(propertyContent);

            // Resolve additional properties
            themeProperty.setThemePath(themePath.toString());
            themeProperty.setFolderName(themePath.getFileName().toString());
            themeProperty.setHasOptions(hasOptions(themePath));
            themeProperty.setActivated(false);

            // Set screenshots
            getScreenshotsFileName(themePath).ifPresent(screenshotsName -> themeProperty.setScreenshots(StringUtils.join("/themes/",
                    FilenameUtils.getBasename(themeProperty.getThemePath()),
                    "/",
                    screenshotsName)));

            return Optional.of(themeProperty);
        } catch (Exception e) {
            log.warn("Failed to load theme property file", e);
        }
        return Optional.empty();
    }

    /**
     * Gets screenshots file name.
     *
     * @param themePath theme path must not be null
     * @return screenshots file name or null if the given theme path has not screenshots
     * @throws IOException throws when listing files
     */
    @NonNull
    private Optional<String> getScreenshotsFileName(@NonNull Path themePath) throws IOException {
        Assert.notNull(themePath, "Theme path must not be null");

        try (Stream<Path> pathStream = Files.list(themePath)) {
            return pathStream.filter(path -> Files.isRegularFile(path)
                    && Files.isReadable(path)
                    && FilenameUtils.getBasename(path.toString()).equalsIgnoreCase(THEME_SCREENSHOTS_NAME))
                    .findFirst()
                    .map(path -> path.getFileName().toString());
        }
    }

    /**
     * Gets property path of nullable.
     *
     * @param themePath theme path.
     * @return an optional property path
     */
    @NonNull
    private Optional<Path> fetchPropertyPath(@NonNull Path themePath) {
        Assert.notNull(themePath, "Theme path must not be null");

        for (String propertyPathName : THEME_PROPERTY_FILE_NAMES) {
            Path propertyPath = themePath.resolve(propertyPathName);

            log.debug("Attempting to find property file: [{}]", propertyPath);
            if (Files.exists(propertyPath) && Files.isReadable(propertyPath)) {
                log.debug("Found property file: [{}]", propertyPath);
                return Optional.of(propertyPath);
            }
        }

        log.warn("Property file was not found in [{}]", themePath);

        return Optional.empty();
    }

    /**
     * Check existence of the options.
     *
     * @param themePath theme path must not be null
     * @return true if it has options; false otherwise
     */
    private boolean hasOptions(@NonNull Path themePath) {
        Assert.notNull(themePath, "Path must not be null");

        for (String optionsName : SETTINGS_NAMES) {
            // Resolve the options path
            Path optionsPath = themePath.resolve(optionsName);

            log.debug("Check options file for path: [{}]", optionsPath);

            if (Files.exists(optionsPath)) {
                return true;
            }
        }
        return false;
    }
}
