package run.halo.app.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.handler.theme.config.support.Group;
import run.halo.app.handler.theme.config.support.Item;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.support.ThemeFile;

/**
 * Theme service interface.
 *
 * @author ryanwang
 * @author guqing
 * @date 2019-03-26
 */
public interface ThemeService {

    /**
     * Configuration file name.
     */
    String[] SETTINGS_NAMES = {"settings.yaml", "settings.yml"};

    /**
     * The type of file that can be modified.
     */
    String[] CAN_EDIT_SUFFIX = {".ftl", ".css", ".js", ".yaml", ".yml", ".properties"};

    /**
     * Theme folder location.
     */
    String THEME_FOLDER = "templates/themes";

    /**
     * Theme cache key.
     */
    String THEMES_CACHE_KEY = "themes";

    /**
     * Custom sheet template prefix.
     */
    String CUSTOM_SHEET_PREFIX = "sheet_";

    /**
     * Custom post template prefix.
     */
    String CUSTOM_POST_PREFIX = "post_";

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
     * @param themeId theme id
     * @return a optional theme property
     */
    @NonNull
    Optional<ThemeProperty> fetchThemePropertyBy(@Nullable String themeId);

    /**
     * Gets all themes.
     *
     * @return set of themes
     */
    @NonNull
    List<ThemeProperty> getThemes();

    /**
     * Lists theme folder by theme name.
     *
     * @param themeId theme id
     * @return theme file list
     */
    @NonNull
    List<ThemeFile> listThemeFolderBy(@NonNull String themeId);

    /**
     * Lists a set of custom template, such as sheet_xxx.ftl/post_xxx.ftl, and xxx will be
     * template name
     *
     * @param themeId theme id must not be blank
     * @param prefix post_ or sheet_
     * @return a set of templates
     */
    @NonNull
    List<String> listCustomTemplates(@NonNull String themeId, @NonNull String prefix);

    /**
     * Judging whether template exists under the specified theme.
     *
     * @param template template must not be blank
     * @return boolean
     */
    boolean templateExists(@Nullable String template);

    /**
     * Checks whether theme exists under template path.
     *
     * @param themeId theme id
     * @return boolean
     */
    boolean themeExists(@Nullable String themeId);

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
     * Gets template content by template absolute path and themeId.
     *
     * @param themeId themeId
     * @param absolutePath absolute path
     * @return template content
     */
    String getTemplateContent(@NonNull String themeId, @NonNull String absolutePath);

    /**
     * Saves template content by template absolute path.
     *
     * @param absolutePath absolute path
     * @param content new content
     */
    void saveTemplateContent(@NonNull String absolutePath, @NonNull String content);

    /**
     * Saves template content by template absolute path and themeId.
     *
     * @param themeId themeId
     * @param absolutePath absolute path
     * @param content new content
     */
    void saveTemplateContent(@NonNull String themeId, @NonNull String absolutePath,
        @NonNull String content);

    /**
     * Deletes a theme by key.
     *
     * @param themeId theme id must not be blank
     * @param deleteSettings whether all settings of the specified theme should be deleted.
     */
    void deleteTheme(@NonNull String themeId, @NonNull Boolean deleteSettings);

    /**
     * Fetches theme configuration.
     *
     * @param themeId must not be blank
     * @return theme configuration
     */
    @NonNull
    List<Group> fetchConfig(@NonNull String themeId);

    /**
     * Fetch config items by <code>themeId</code> and <code>group</code>.
     *
     * @param themeId theme id must not be blank
     * @param group group name must not be blank
     * @return config items
     */
    Set<Item> fetchConfigItemsBy(@NonNull String themeId, String group);

    /**
     * Renders a theme page.
     *
     * @param pageName must not be blank
     * @return full path of the theme page
     */
    @NonNull
    String render(@NonNull String pageName);

    /**
     * Renders a theme page.
     *
     * @param pageName must not be blank
     * @return full path of the theme page
     */
    @NonNull
    String renderWithSuffix(@NonNull String pageName);

    /**
     * Gets current theme id.
     *
     * @return current theme id
     */
    @NonNull
    String getActivatedThemeId();

    /**
     * Gets activated theme property.
     *
     * @return activated theme property
     */
    @NonNull
    ThemeProperty getActivatedTheme();

    /**
     * Fetch activated theme property.
     *
     * @return activated theme property
     */
    @NonNull
    Optional<ThemeProperty> fetchActivatedTheme();

    /**
     * Actives a theme.
     *
     * @param themeId theme id must not be blank
     * @return theme property
     */
    @NonNull
    ThemeProperty activateTheme(@NonNull String themeId);

    /**
     * Upload theme.
     *
     * @param file multipart file must not be null
     * @return theme info
     */
    @NonNull
    ThemeProperty upload(@NonNull MultipartFile file);

    /**
     * Fetches a new theme.
     *
     * @param uri theme remote uri must not be null
     * @return theme property
     */
    @NonNull
    ThemeProperty fetch(@NonNull String uri);

    /**
     * Reloads themes
     */
    void reload();

    /**
     * Updates theme by theme id.
     *
     * @param themeId theme id must not be blank
     * @return theme property
     */
    @NonNull
    ThemeProperty update(@NonNull String themeId);

    /**
     * Updates theme by theme id.
     *
     * @param themeId theme id must not be blank
     * @param file multipart file must not be null
     * @return theme info
     */
    ThemeProperty update(@NonNull String themeId, @NonNull MultipartFile file);
}
