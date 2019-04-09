package run.halo.app.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.model.support.ThemeProperty;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2019/3/26
 */
public interface ThemeService {

    /**
     * Theme property file name.
     */
    String THEME_PROPERTY_FILE_NAME = "theme.properties";


    /**
     * Configuration file name.
     */
    String[] OPTIONS_NAMES = {"options.yaml", "options.yml"};

    /**
     * The type of file that can be modified.
     */
    String[] CAN_EDIT_SUFFIX = {"ftl", "css", "js"};

    /**
     * These file names cannot be displayed.
     */
    String[] FILTER_FILES = {".git", ".DS_Store", THEME_PROPERTY_FILE_NAME, "options.yaml", "option.yml"};

    /**
     * Theme folder location.
     */
    String THEME_FOLDER = "templates/themes";

    /**
     * Theme screenshots name.
     */
    String THEME_SCREENSHOTS_NAME = "screenshot";


    /**
     * Render template.
     */
    String RENDER_TEMPLATE = "themes/%s/%s";

    /**
     * Theme cache key.
     */
    String THEMES_CACHE_KEY = "themes";

    /**
     * Get theme property by theme id.
     *
     * @param themeId must not be blank
     * @return theme property
     */
    @NonNull
    ThemeProperty getThemeOfNonNullBy(@NonNull String themeId);

    /**
     * Get theme property by theme id.
     *
     * @param themeId must not be blank
     * @return a optional theme property
     */
    @NonNull
    Optional<ThemeProperty> getThemeBy(@NonNull String themeId);

    /**
     * Gets all themes
     *
     * @return list of themes
     */
    List<ThemeProperty> getThemes();

    /**
     * Lists theme folder by absolute path.
     *
     * @param absolutePath absolutePath
     * @return List<ThemeFile>
     */
    List<ThemeFile> listThemeFolder(@NonNull String absolutePath);

    /**
     * Lists theme folder by theme name.
     *
     * @param themeId theme id
     * @return List<ThemeFile>
     */
    List<ThemeFile> listThemeFolderBy(@NonNull String themeId);

    /**
     * Gets custom template, such as page_xxx.ftl, and xxx will be template name
     *
     * @param themeId theme id
     * @return List
     */
    List<String> getCustomTpl(@NonNull String themeId);

    /**
     * Judging whether template exists under the specified theme
     *
     * @param template template
     * @return boolean
     */
    boolean isTemplateExist(@NonNull String template);

    /**
     * Checks whether theme exists under template path
     *
     * @param themeId theme name
     * @return boolean
     */
    boolean isThemeExist(@NonNull String themeId);

    /**
     * Gets theme base path.
     *
     * @return File
     */
    File getThemeBasePath();

    /**
     * Gets theme base path.
     *
     * @return theme base path
     */
    Path getBasePath();

    /**
     * Gets template content by template absolute path.
     *
     * @param absolutePath absolute path
     * @return template content
     */
    String getTemplateContent(@NonNull String absolutePath);

    /**
     * Saves template content by template absolute path.
     *
     * @param absolutePath absolute path
     * @param content      new content
     */
    void saveTemplateContent(@NonNull String absolutePath, @NonNull String content);

    /**
     * Deletes a theme by key.
     *
     * @param themeId theme id must not be blank
     */
    void deleteTheme(@NonNull String themeId);

    /**
     * Fetches theme configuration.
     *
     * @param themeId must not be blank
     * @return theme configuration or null if not found
     */
    @Nullable
    Object fetchConfig(@NonNull String themeId);

    /**
     * Renders a theme page.
     *
     * @param pageName must not be blank
     * @return full path of the theme page
     */
    @NonNull
    String render(@NonNull String pageName);

    /**
     * Gets current theme id.
     *
     * @return current theme id
     */
    @NonNull
    String getActivatedThemeId();

    /**
     * Actives a theme.
     *
     * @param themeId theme id must not be blank
     * @return theme property
     */
    @NonNull
    ThemeProperty activeTheme(@NonNull String themeId);
}
