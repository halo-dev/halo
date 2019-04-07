package run.halo.app.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import org.springframework.stereotype.Service;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.support.Theme;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.model.support.ThemeProperties;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019/3/26
 */
@Service
public class ThemeServiceImpl implements ThemeService {

    /**
     * The type of file that can be modified.
     */
    private static String[] CAN_EDIT_SUFFIX = {"ftl", "css", "js"};

    /**
     * These file names cannot be displayed.
     */
    private static String[] FILTER_FILES = {".git", ".DS_Store", "theme.properties"};

    private final HaloProperties haloProperties;

    public ThemeServiceImpl(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    /**
     * Gets all themes
     *
     * @return list of themes
     */
    @Override
    public List<Theme> getThemes() {
        final List<Theme> themes = new ArrayList<>();
        final File[] files = getThemeBasePath().listFiles();
        try {
            if (null != files) {
                Theme theme;
                for (File file : files) {
                    if (!file.isDirectory()) {
                        continue;
                    }
                    theme = new Theme();
                    theme.setKey(file.getName());
                    File optionsPath = new File(file.getAbsolutePath(), "module/options.ftl");
                    theme.setHasOptions(optionsPath.exists());
                    theme.setProperties(getProperties(new File(getThemeBasePath(), file.getName())));
                    themes.add(theme);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Themes scan failed", e);
        }
        return themes;
    }

    /**
     * Lists theme folder by absolute path.
     *
     * @param absolutePath absolutePath
     * @return List<ThemeFile>
     */
    @Override
    public List<ThemeFile> listThemeFolder(String absolutePath) {
        final List<ThemeFile> templates = new ArrayList<>();
        try {
            File absolutePathFile = new File(absolutePath);
            File[] baseFiles = absolutePathFile.listFiles();
            if (null != baseFiles) {
                baseFileFor:
                for (File base : baseFiles) {
                    for (String filterFile : FILTER_FILES) {
                        if (filterFile.equals(base.getName())) {
                            continue baseFileFor;
                        }
                    }
                    ThemeFile file = new ThemeFile();
                    if (base.isDirectory()) {
                        file.setName(base.getName());
                        file.setPath(base.getAbsolutePath());
                        file.setIsFile(false);
                        file.setNode(listThemeFolder(base.getAbsolutePath()));
                    } else {
                        file.setName(base.getName());
                        file.setPath(base.getAbsolutePath());
                        file.setIsFile(true);
                        for (String suffix : CAN_EDIT_SUFFIX) {
                            if (FilenameUtils.getExtension(file.getName()).equals(suffix)) {
                                file.setCanEdit(true);
                                break;
                            } else {
                                file.setCanEdit(false);
                            }
                        }
                    }
                    templates.add(file);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get theme template", e);
        }
        templates.sort(Comparator.comparing(ThemeFile::getIsFile));
        return templates;
    }

    /**
     * Lists theme folder by theme name.
     *
     * @param theme theme
     * @return List<ThemeFile>
     */
    @Override
    public List<ThemeFile> listThemeFolderBy(String theme) {
        File themePath = new File(getThemeBasePath(), theme);
        return listThemeFolder(themePath.getAbsolutePath());
    }

    /**
     * Gets custom template, such as page_xxx.ftl, and xxx will be template name
     *
     * @param theme theme name
     * @return List
     */
    @Override
    public List<String> getCustomTpl(String theme) {
        final List<String> templates = new ArrayList<>();
        final File themePath = new File(getThemeBasePath(), theme);
        final File[] themeFiles = themePath.listFiles();
        if (null != themeFiles && themeFiles.length > 0) {
            for (File file : themeFiles) {
                String[] split = StrUtil.removeSuffix(file.getName(), HaloConst.SUFFIX_FTL).split("_");
                if (split.length == 2 && "page".equals(split[0])) {
                    templates.add(StrUtil.removeSuffix(file.getName(), HaloConst.SUFFIX_FTL));
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
    @Override
    public boolean isTemplateExist(String template) {
        StrBuilder templatePath = new StrBuilder(HaloConst.ACTIVATED_THEME_NAME);
        templatePath.append("/");
        templatePath.append(template);
        File file = new File(getThemeBasePath(), templatePath.toString());
        return file.exists();
    }

    /**
     * Judging whether theme exists under template path
     *
     * @param theme theme name
     * @return boolean
     */
    @Override
    public boolean isThemeExist(String theme) {
        File file = new File(getThemeBasePath(), theme);
        return file.exists();
    }

    /**
     * Gets theme base path.
     *
     * @return File
     */
    @Override
    public File getThemeBasePath() {
        return new File(haloProperties.getWorkDir(), "templates/themes");
    }

    /**
     * Get theme Properties.
     *
     * @param path path
     * @return ThemeProperties
     */
    @Override
    public ThemeProperties getProperties(File path) {
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

    /**
     * Get template content by template absolute path.
     *
     * @param absolutePath absolute path
     * @return template content
     */
    @Override
    public String getTemplateContent(String absolutePath) {
        final FileReader fileReader = new FileReader(absolutePath);
        return fileReader.readString();
    }

    /**
     * Save template content by template absolute path.
     *
     * @param absolutePath absolute path
     * @param content      new content
     */
    @Override
    public void saveTemplateContent(String absolutePath, String content) {
        final FileWriter fileWriter = new FileWriter(absolutePath);
        fileWriter.write(content);
    }

    /**
     * Delete a theme by key.
     *
     * @param key theme key
     */
    @Override
    public void deleteTheme(String key) {
        File file = new File(this.getThemeBasePath(),key);
        FileUtil.del(file);
        HaloConst.THEMES = this.getThemes();
    }
}
