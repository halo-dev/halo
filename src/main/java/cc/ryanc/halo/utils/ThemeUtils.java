package cc.ryanc.halo.utils;

import cc.ryanc.halo.model.support.HaloConst;
import cc.ryanc.halo.model.support.Theme;
import cc.ryanc.halo.model.support.ThemeProperties;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Theme utils
 *
 * @author : RYAN0UP
 * @date : 2019/3/16
 */
@Slf4j
public class ThemeUtils {

    /**
     * Scan internal themes and user's themes
     *
     * @return List
     */
    public static List<Theme> getThemes() {
        final List<Theme> themes = new ArrayList<>();
        try {
            themes.addAll(getThemesByPath(getInternalThemesBasePath(), true));
            themes.addAll(getThemesByPath(getUsersThemesBasePath(), false));
        } catch (Exception e) {
            throw new RuntimeException("Themes scan failed", e);
        }
        return themes;
    }

    /**
     * Scan themes by directory
     *
     * @param themesPath themes base Path
     * @param isInternal isInternal
     * @return themes
     */
    private static List<Theme> getThemesByPath(File themesBasePath, boolean isInternal) {
        final List<Theme> themes = new ArrayList<>();
        try {
            final File[] files = themesBasePath.listFiles();
            if (null != files) {
                Theme theme;
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (StrUtil.equals("__MACOSX", file.getName())) {
                            continue;
                        }
                        theme = new Theme();
                        theme.setThemeDir(file.getName());
                        File optionsPath = new File(themesBasePath.getAbsolutePath(),
                                file.getName() + "/module/options.ftl");
                        if (optionsPath.exists()) {
                            theme.setHasOptions(true);
                        } else {
                            theme.setHasOptions(false);
                        }
                        File gitPath = new File(themesBasePath.getAbsolutePath(), file.getName() + "/.git");
                        if (gitPath.exists()) {
                            theme.setHasUpdate(true);
                        } else {
                            theme.setHasUpdate(false);
                        }
                        theme.setProperties(getProperties(new File(themesBasePath,file.getName())));
                        theme.setInternal(isInternal);
                        themes.add(theme);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Themes scan failed", e);
        }
        return themes;
    }

    /**
     * Get internal themes base path
     *
     * @return File
     * @throws FileNotFoundException FileNotFoundException
     */
    public static File getInternalThemesBasePath() throws FileNotFoundException {
        return new File(ResourceUtils.getURL("classpath:").getPath(), "templates/themes");
    }

    /**
     * Get user's themes base path
     *
     * @return File
     */
    public static File getUsersThemesBasePath() {
        return new File(System.getProperties().getProperty("user.home"), "halo/templates/themes");
    }

    /**
     * Get themes path by theme name
     *
     * @param themeName themeName
     * @return File
     */
    public static File getThemesBasePath(String themeName) throws FileNotFoundException {
        return isInternal(themeName) ? getInternalThemesBasePath() : getUsersThemesBasePath();
    }

    /**
     * Get theme templates
     *
     * @param theme theme
     * @return List<String>
     */
    public static List<String> getTemplates(String theme) {
        final List<String> templates = new ArrayList<>();
        try {
            final File themesPath = new File(getThemesBasePath(theme), theme);
            final File modulePath = new File(themesPath.getAbsolutePath(), "module");
            final File[] baseFiles = themesPath.listFiles();
            final File[] moduleFiles = modulePath.listFiles();
            if (null != moduleFiles) {
                for (File file : moduleFiles) {
                    if (file.isFile() && file.getName().endsWith(".ftl")) {
                        templates.add("module/" + file.getName());
                    }
                }
            }
            if (null != baseFiles) {
                for (File file : baseFiles) {
                    if (file.isFile() && file.getName().endsWith(".ftl")) {
                        templates.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get theme template", e);
        }
        return templates;
    }

    /**
     * Get custom template, such as page_xxx.ftl, and xxx will be template name
     *
     * @param theme theme name
     * @return List
     */
    public static List<String> getCustomTpl(String theme) throws FileNotFoundException {
        final List<String> templates = new ArrayList<>();
        final File themePath = new File(getThemesBasePath(theme), theme);
        final File[] themeFiles = themePath.listFiles();
        if (null != themeFiles && themeFiles.length > 0) {
            for (File file : themeFiles) {
                String[] split = StrUtil.removeSuffix(file.getName(), ".ftl").split("_");
                if (split.length == 2 && "page".equals(split[0])) {
                    templates.add(StrUtil.removeSuffix(file.getName(), ".ftl"));
                }
            }
        }
        return templates;
    }

    /**
     * Judging whether template exists under the specified theme
     *
     * @param template template
     * @return boolean
     */
    public static boolean isTemplateExist(String template) throws FileNotFoundException {
        boolean result = false;
        StrBuilder templatePath = new StrBuilder(BaseContentController.THEME);
        templatePath.append("/");
        templatePath.append(template);
        File file = new File(getThemesBasePath(BaseContentController.THEME), templatePath.toString());
        if (file.exists()) {
            result = true;
        }
        return result;
    }

    /**
     * Judging whether theme exists under template path
     *
     * @param theme theme name
     * @return boolean
     * @throws FileNotFoundException FileNotFoundException
     */
    public static boolean isThemeExist(String theme) throws FileNotFoundException {
        boolean result = false;
        File file = new File(getThemesBasePath(theme), theme);
        if (file.exists()) {
            result = true;
        }
        return result;
    }

    /**
     * Judging whether the theme is a internal theme or not
     *
     * @param themeName themeName
     * @return boolean
     */
    public static boolean isInternal(String themeName) {
        boolean result = false;
        List<Theme> themes = HaloConst.THEMES;
        for (Theme theme : themes) {
            if (theme.getThemeDir().equals(themeName) && theme.isInternal()) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Get theme Properties.
     *
     * @param path path
     * @return ThemeProperties
     */
    public static ThemeProperties getProperties(File path) {
        File propertiesFile = new File(path, "theme.properties");
        ThemeProperties properties = new ThemeProperties();
        if (propertiesFile.exists()) {
            Props props = new Props(propertiesFile);
            properties.setId(props.getStr("theme.id"));
            properties.setName(props.getStr("theme.name"));
            properties.setWebsite(props.getStr("theme.website"));
            properties.setDescription(props.getStr("theme.description"));
            properties.setLogo(props.getStr("theme.logo"));
            properties.setVersion(props.getStr("theme.version"));
            properties.setAuthor(props.getStr("theme.author"));
            properties.setAuthorWebsite(props.getStr("theme.author.website"));
        }
        return properties;
    }
}
