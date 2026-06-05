package run.halo.app.theme;

import java.nio.file.Files;
import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import run.halo.app.infra.utils.FileUtils;

public final class ThemeUiResources {

    public static final String UI_LOCATION = "ui";
    public static final String JS_BUNDLE = "main.js";
    public static final String CSS_BUNDLE = "style.css";
    public static final String MODULE_NAME_PREFIX = "theme:";

    private ThemeUiResources() {}

    public static @Nullable Resource getBundleResource(Path themeRoot, String themeName, String bundleName) {
        return getResource(themeRoot, themeName, bundleName);
    }

    public static @Nullable Resource getResource(Path themeRoot, String themeName, String resourcePath) {
        Assert.notNull(themeRoot, "Theme root must not be null");
        Assert.hasText(themeName, "Theme name must not be blank");
        Assert.hasText(resourcePath, "Resource path must not be blank");

        var cleanedResourcePath = StringUtils.cleanPath(resourcePath);
        while (cleanedResourcePath.startsWith("/")) {
            cleanedResourcePath = cleanedResourcePath.substring(1);
        }
        if (!StringUtils.hasText(cleanedResourcePath)) {
            return null;
        }
        var uiRoot = themeRoot
                .resolve(themeName)
                .resolve(UI_LOCATION)
                .toAbsolutePath()
                .normalize();
        var resourcePathToCheck =
                uiRoot.resolve(cleanedResourcePath).toAbsolutePath().normalize();
        FileUtils.checkDirectoryTraversal(uiRoot, resourcePathToCheck);
        if (!Files.isRegularFile(resourcePathToCheck) || !Files.isReadable(resourcePathToCheck)) {
            return null;
        }
        return new FileSystemResource(resourcePathToCheck);
    }

    public static String buildModuleName(String themeName) {
        Assert.hasText(themeName, "Theme name must not be blank");
        return MODULE_NAME_PREFIX + themeName;
    }

    public static String buildAssetUrl(String themeName, String resourceName, @Nullable String version) {
        Assert.hasText(themeName, "Theme name must not be blank");
        Assert.hasText(resourceName, "Resource name must not be blank");

        var builder = UriComponentsBuilder.fromPath("/themes/{themeName}/ui/assets/{resourceName}");
        if (StringUtils.hasText(version)) {
            builder.queryParam("v", version);
        }
        return builder.buildAndExpand(themeName, resourceName).encode().toUriString();
    }
}
