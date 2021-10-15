package run.halo.app.theme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.handler.theme.config.ThemePropertyResolver;
import run.halo.app.handler.theme.config.impl.YamlThemePropertyResolver;
import run.halo.app.handler.theme.config.support.ThemeProperty;

/**
 * Theme property scanner.
 *
 * @author johnniang
 */
@Slf4j
public enum ThemePropertyScanner {

    INSTANCE;

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
     * @param themeRootPath theme root path must not be null
     * @return an optional theme property
     */
    @NonNull
    public Optional<ThemeProperty> fetchThemeProperty(@NonNull Path themeRootPath) {
        Assert.notNull(themeRootPath, "Theme path must not be null");

        return ThemeMetaLocator.INSTANCE.locateProperty(themeRootPath).map(propertyPath -> {
            final var rootPath = propertyPath.getParent();
            try {
                // Get property content
                final var propertyContent = Files.readString(propertyPath);

                // Resolve the base properties
                final var themeProperty = propertyResolver.resolve(propertyContent);

                // Resolve additional properties
                themeProperty.setThemePath(rootPath.toString());
                themeProperty.setFolderName(rootPath.getFileName().toString());
                themeProperty.setHasOptions(hasOptions(rootPath));
                themeProperty.setActivated(false);

                // resolve screenshot
                ThemeMetaLocator.INSTANCE.locateScreenshot(rootPath).ifPresent(screenshotPath -> {
                    final var screenshotRelPath = StringUtils.join("/themes/",
                            themeProperty.getFolderName(),
                            "/",
                            screenshotPath.getFileName().toString());
                    themeProperty.setScreenshots(screenshotRelPath);
                });
                return themeProperty;
            } catch (Exception e) {
                log.warn("Failed to load theme property file", e);
            }
            return null;
        });
    }

    /**
     * Check existence of the options.
     *
     * @param themePath theme path must not be null
     * @return true if it has options; false otherwise
     */
    private boolean hasOptions(@NonNull Path themePath) {
        Assert.notNull(themePath, "Path must not be null");

        return ThemeMetaLocator.INSTANCE.locateSetting(themePath).isPresent();
    }
}
