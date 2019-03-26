package cc.ryanc.halo.service;

import cc.ryanc.halo.model.support.Theme;
import cc.ryanc.halo.model.support.ThemeProperties;

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
     * Gets theme templates
     *
     * @param theme theme
     * @return List<String>
     */
    List<String> getTemplates(String theme);

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
}
