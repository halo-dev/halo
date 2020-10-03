package run.halo.app.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.handler.theme.config.support.Group;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.support.ThemeFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Theme service interface.
 *
 * @author ryanwang
 * @date 2019-03-26
 */
public interface ThemeService {

    /**
     * Theme property file name.
     */
    @Deprecated
    String THEME_PROPERTY_FILE_NAME = "theme.yaml";

    /**
     * Theme property file name.
     */
    @Deprecated
    String[] THEME_PROPERTY_FILE_NAMES = {"theme.yaml", "theme.yml"};


    /**
     * Configuration file name.
     */
    String[] SETTINGS_NAMES = {"settings.yaml", "settings.yml"};

    /**
     * The type of file that can be modified.
     */
    String[] CAN_EDIT_SUFFIX = {".ftl", ".css", ".js", ".yaml", ".yml", ".properties"};

    /**
     * These file names cannot be displayed.
     */
    String[] FILTER_FILES = {".git", ".DS_Store", "theme.yaml", "theme.yml", "settings.yaml", "settings.yml"};

    /**
     * Theme folder location.
     */
    String THEME_FOLDER = "templates/themes";

    /**
     * Theme screenshots name.
     */
    @Deprecated
    String THEME_SCREENSHOTS_NAME = "screenshot";


    /**
     * Render template.
     */
    String RENDER_TEMPLATE = "themes/%s/%s";

    /**
     * Render template with suffix.
     */
    String RENDER_TEMPLATE_SUFFIX = "themes/%s/%s.ftl";

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
     * Theme provider remote name.
     */
    String THEME_PROVIDER_REMOTE_NAME = "origin";

    /**
     * Default remote branch name.
     */
    String DEFAULT_REMOTE_BRANCH = "master";

    /**
     * Key to access the zip file url which is in the http response
     */
    String ZIP_FILE_KEY = "zipball_url";

    /**
     * Key to access the tag name which is in the http response
     */
    String TAG_KEY = "tag_name";

    /**
     * Get theme property by theme id.
     *
     * @param themeId must not be blank
     * @return theme property
     */
    @NonNull
    @Deprecated
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
     * Gets all themes
     *
     * @return set of themes
     */
    @NonNull
    List<ThemeProperty> getThemes();

    /**
     * Lists theme folder by theme name.
     *
     * @param themeId theme id
     * @return List<ThemeFile>
     */
    @NonNull
    List<ThemeFile> listThemeFolderBy(@NonNull String themeId);

    /**
     * Lists a set of custom template, such as sheet_xxx.ftl, and xxx will be template name
     *
     * @param themeId theme id must not be blank
     * @return a set of templates
     */
    @Deprecated
    @NonNull
    List<String> listCustomTemplates(@NonNull String themeId);

    /**
     * Lists a set of custom template, such as sheet_xxx.ftl/post_xxx.ftl, and xxx will be template name
     *
     * @param themeId theme id must not be blank
     * @param prefix  post_ or sheet_
     * @return a set of templates
     */
    @NonNull
    List<String> listCustomTemplates(@NonNull String themeId, @NonNull String prefix);

    /**
     * Judging whether template exists under the specified theme
     *
     * @param template template must not be blank
     * @return boolean
     */
    boolean templateExists(@Nullable String template);

    /**
     * Checks whether theme exists under template path
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
     * @param themeId      themeId
     * @param absolutePath absolute path
     * @return template content
     */
    String getTemplateContent(@NonNull String themeId, @NonNull String absolutePath);

    /**
     * Saves template content by template absolute path.
     *
     * @param absolutePath absolute path
     * @param content      new content
     */
    void saveTemplateContent(@NonNull String absolutePath, @NonNull String content);

    /**
     * Saves template content by template absolute path and themeId.
     *
     * @param themeId      themeId
     * @param absolutePath absolute path
     * @param content      new content
     */
    void saveTemplateContent(@NonNull String themeId, @NonNull String absolutePath, @NonNull String content);

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
     * @return theme configuration
     */
    @NonNull
    List<Group> fetchConfig(@NonNull String themeId);

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
     * Adds a new theme.
     *
     * @param themeTmpPath theme temporary path must not be null
     * @return theme property
     * @throws IOException IOException
     */
    @NonNull
    ThemeProperty add(@NonNull Path themeTmpPath) throws IOException;

    /**
     * Fetches a new theme.
     *
     * @param uri theme remote uri must not be null
     * @return theme property
     */
    @NonNull
    ThemeProperty fetch(@NonNull String uri);

    /**
     * Fetches the latest release
     *
     * @param uri theme remote uri must not be null
     * @return theme property
     */
    @NonNull
    ThemeProperty fetchLatestRelease(@NonNull String uri);

    /**
     * Fetches all the branches info
     *
     * @param uri theme remote uri must not be null
     * @return list of theme properties
     */
    @NonNull
    List<ThemeProperty> fetchBranches(@NonNull String uri);

    /**
     * Fetches all the release info
     *
     * @param uri theme remote uri must not be null
     * @return list of theme properties
     */
    @NonNull
    List<ThemeProperty> fetchReleases(@NonNull String uri);

    /**
     * Fetches a specific release
     *
     * @param uri     theme remote uri must not be null
     * @param tagName release tag name must not be null
     * @return theme property
     */
    @NonNull
    ThemeProperty fetchRelease(@NonNull String uri, @NonNull String tagName);

    /**
     * Fetches a specific branch (clone)
     *
     * @param uri        theme remote uri must not be null
     * @param branchName wanted branch must not be null
     * @return theme property
     */
    @NonNull
    ThemeProperty fetchBranch(@NonNull String uri, @NonNull String branchName);

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
     * @param file    multipart file must not be null
     * @return theme info
     */
    ThemeProperty update(@NonNull String themeId, @NonNull MultipartFile file);
}
