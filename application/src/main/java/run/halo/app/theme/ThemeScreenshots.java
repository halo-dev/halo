package run.halo.app.theme;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.springframework.web.util.UriComponentsBuilder;

public final class ThemeScreenshots {

    private static final List<String> SUPPORTED_FILENAMES =
            List.of("screenshot.png", "screenshot.jpeg", "screenshot.jpg", "screenshot.webp");

    private ThemeScreenshots() {}

    public static Optional<Path> findScreenshot(Path themePath) {
        return SUPPORTED_FILENAMES.stream()
                .map(themePath::resolve)
                .filter(Files::isRegularFile)
                .filter(Files::isReadable)
                .findFirst();
    }

    public static boolean isSupportedFilename(String filename) {
        return SUPPORTED_FILENAMES.contains(filename);
    }

    public static String buildScreenshotUrl(String themeName, Path screenshotPath) {
        return UriComponentsBuilder.newInstance()
                .pathSegment("themes", themeName, screenshotPath.getFileName().toString())
                .build()
                .toString();
    }
}
