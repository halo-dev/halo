package cc.ryanc.halo.utils;

import cc.ryanc.halo.model.support.Theme;
import cn.hutool.core.util.StrUtil;
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
            themes.addAll(getThemesByPath(getInternalThemesPath()));
            themes.addAll(getThemesByPath(getUsersThemesPath()));
        } catch (Exception e) {
            log.error("Themes scan failed", e);
        }
        return themes;
    }

    /**
     * Scan themes by directory
     *
     * @param file file
     * @return List<Theme>
     */
    private static List<Theme> getThemesByPath(File themesPath) {
        final List<Theme> themes = new ArrayList<>();
        try {
            final File[] files = themesPath.listFiles();
            if (null != files) {
                Theme theme;
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (StrUtil.equals("__MACOSX", file.getName())) {
                            continue;
                        }
                        theme = new Theme();
                        theme.setThemeName(file.getName());
                        File optionsPath = new File(themesPath.getAbsolutePath(),
                                file.getName() + "/module/options.ftl");
                        if (optionsPath.exists()) {
                            theme.setHasOptions(true);
                        } else {
                            theme.setHasOptions(false);
                        }
                        File gitPath = new File(themesPath.getAbsolutePath(), file.getName() + "/.git");
                        if (gitPath.exists()) {
                            theme.setHasUpdate(true);
                        } else {
                            theme.setHasUpdate(false);
                        }
                        themes.add(theme);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Themes scan failed", e);
        }
        return themes;
    }

    /**
     * Get internal themes
     *
     * @return File
     * @throws FileNotFoundException FileNotFoundException
     */
    public static File getInternalThemesPath() throws FileNotFoundException {
        return new File(ResourceUtils.getURL("classpath:").getPath(), "templates/themes");
    }

    /**
     * Get user's themes
     *
     * @return File
     */
    public static File getUsersThemesPath() {
        return new File(System.getProperties().getProperty("user.home"), "halo/templates/themes");
    }

    /**
     * Get theme templates
     *
     * @param theme theme
     * @return List<String>
     */
    public static List<String> getTplName(String theme) {
        final List<String> templates = new ArrayList<>();
        try {
            final File themesPath = new File(getUsersThemesPath(), "templates/themes/" + theme);
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
            log.error("Failed to get theme template", e);
        }
        return templates;
    }

    /**
     * Get custom template, such as page_xxx.ftl, and xxx will be template name
     *
     * @return List
     */
    public static List<String> getCustomTpl(String theme) {
        final List<String> templates = new ArrayList<>();
        final File themePath = new File(getUsersThemesPath(), "templates/themes/" + theme);
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
}
