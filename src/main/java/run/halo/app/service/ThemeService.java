package run.halo.app.service;

import run.halo.app.model.support.Theme;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.model.support.ThemeProperties;

import java.io.File;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019/3/26
 */
public interface ThemeService {

    /**
     * Gets all themes
     *
     * @return list of themes
     */
    List<Theme> getThemes();

    /**
     * Lists theme folder by absolute path.
     *
     * @param absolutePath absolutePath
     * @return List<ThemeFile>
     */
    List<ThemeFile> listThemeFolder(String absolutePath);

    /**
     * Lists theme folder by theme name.
     *
     * @param theme theme
     * @return List<ThemeFile>
     */
    List<ThemeFile> listThemeFolderBy(String theme);

    /**
     * Gets custom template, such as page_xxx.ftl, and xxx will be template name
     *
     * @param theme theme name
     * @return List
     */
    List<String> getCustomTpl(String theme);

    /**
     * Judging whether template exists under the specified theme
     *
     * @param template template
     * @return boolean
     */
    boolean isTemplateExist(String template);

    /**
     * Judging whether theme exists under template path
     *
     * @param theme theme name
     * @return boolean
     */
    boolean isThemeExist(String theme);

    /**
     * Gets theme base path.
     *
     * @return File
     */
    File getThemeBasePath();

    /**
     * Get theme Properties.
     *
     * @param path path
     * @return ThemeProperties
     */
    ThemeProperties getProperties(File path);

    /**
     * Get template content by template absolute path.
     *
     * @param absolutePath absolute path
     * @return template content
     */
    String getTemplateContent(String absolutePath);

    /**
     * Save template content by template absolute path.
     *
     * @param absolutePath absolute path
     * @param content      new content
     */
    void saveTemplateContent(String absolutePath, String content);

    /**
     * Delete a theme by key.
     *
     * @param key theme key
     */
    void deleteTheme(String key);
}
