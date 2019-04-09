package run.halo.app.service.impl;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.model.support.ThemeProperty;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static run.halo.app.model.support.HaloConst.DEFAULT_THEME_NAME;

/**
 * @author : RYAN0UP
 * @date : 2019/3/26
 */
@Slf4j
@Service
public class ThemeServiceImpl implements ThemeService {

    /**
     * Theme work directory.
     */
    private final Path workDir;

    private final ObjectMapper yamlMapper;

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    private final Configuration configuration;

    public ThemeServiceImpl(HaloProperties haloProperties,
                            OptionService optionService,
                            StringCacheStore cacheStore,
                            Configuration configuration) {
        this.optionService = optionService;
        this.cacheStore = cacheStore;
        this.configuration = configuration;
        yamlMapper = new ObjectMapper(new YAMLFactory());
        workDir = Paths.get(haloProperties.getWorkDir(), THEME_FOLDER);
    }

    @Override
    public ThemeProperty getThemeOfNonNullBy(String themeId) {
        return getThemeBy(themeId).orElseThrow(() -> new NotFoundException("Theme with id: " + themeId + " was not found").setErrorData(themeId));
    }

    @Override
    public Optional<ThemeProperty> getThemeBy(String themeId) {
        Assert.hasText(themeId, "Theme id must not be blank");

        List<ThemeProperty> themes = getThemes();

        log.debug("Themes type: [{}]", themes.getClass());
        log.debug("Themes: [{}]", themes);

        return themes.stream().filter(themeProperty -> themeProperty.getId().equals(themeId)).findFirst();
    }

    @Override
    public List<ThemeProperty> getThemes() {
        // Fetch themes from cache
        return cacheStore.get(THEMES_CACHE_KEY).map(themesCache -> {
            try {
                // Convert to theme properties
                ThemeProperty[] themeProperties = JsonUtils.jsonToObject(themesCache, ThemeProperty[].class);
                return Arrays.asList(themeProperties);
            } catch (IOException e) {
                throw new ServiceException("Failed to parse json", e);
            }
        }).orElseGet(() -> {
            try {
                // List and filter sub folders
                List<Path> themePaths = Files.list(getBasePath()).filter(path -> Files.isDirectory(path)).collect(Collectors.toList());

                if (CollectionUtils.isEmpty(themePaths)) {
                    return Collections.emptyList();
                }

                // Get theme properties
                List<ThemeProperty> themes = themePaths.stream().map(this::getProperty).collect(Collectors.toList());

                // Cache the themes
                cacheStore.put(THEMES_CACHE_KEY, JsonUtils.objectToJson(themes));

                return themes;
            } catch (Exception e) {
                throw new ServiceException("Themes scan failed", e);
            }
        });
    }

    @Override
    public List<ThemeFile> listThemeFolder(String absolutePath) {
        return listThemeFileTree(Paths.get(absolutePath));
    }

    @Override
    public List<ThemeFile> listThemeFolderBy(String themeId) {
        // Get the theme property
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        // List theme file as tree view
        return listThemeFolder(themeProperty.getThemePath());
    }

    @Override
    public List<String> getCustomTpl(String themeId) {
        // TODO 这里的参数是有问题的，等待修复。
        final List<String> templates = new ArrayList<>();
        final File themePath = new File(getThemeBasePath(), themeId);
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

    @Override
    public boolean isTemplateExist(String template) {
        StrBuilder templatePath = new StrBuilder(getActivatedThemeId());
        templatePath.append("/");
        templatePath.append(template);
        File file = new File(getThemeBasePath(), templatePath.toString());
        return file.exists();
    }

    @Override
    public boolean isThemeExist(String themeId) {
        return getThemeBy(themeId).isPresent();
    }

    @Override
    public File getThemeBasePath() {
        return getBasePath().toFile();
    }

    @Override
    public Path getBasePath() {
        return workDir;
    }

    @Override
    public String getTemplateContent(String absolutePath) {
        // Check the path
        checkDirectory(absolutePath);

        // Read file
        return new FileReader(absolutePath).readString();
    }

    @Override
    public void saveTemplateContent(String absolutePath, String content) {
        // Check the path
        checkDirectory(absolutePath);

        // Write file
        new FileWriter(absolutePath).write(content);
    }

    @Override
    public void deleteTheme(String themeId) {
        // Get the theme property
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        try {
            // Delete the folder
            Files.deleteIfExists(Paths.get(themeProperty.getThemePath()));

            // Delete theme cache
            cacheStore.delete(THEMES_CACHE_KEY);
        } catch (IOException e) {
            throw new ServiceException("Failed to delete theme folder", e).setErrorData(themeId);
        }
    }

    @Override
    public Object fetchConfig(String themeId) {
        Assert.hasText(themeId, "Theme name must not be blank");

        // Get theme property
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        if (!themeProperty.getHasOptions()) {
            // If this theme dose not has an option, then return null
            return null;
        }

        try {
            for (String optionsName : OPTIONS_NAMES) {
                // Resolve the options path
                Path optionsPath = Paths.get(themeProperty.getThemePath(), optionsName);

                log.debug("Finding options in: [{}]", optionsPath.toString());

                // Check existence
                if (!Files.exists(optionsPath)) {
                    continue;
                }

                // Read the yaml file and return the object value
                return yamlMapper.readValue(optionsPath.toFile(), Object.class);
            }

            return null;
        } catch (IOException e) {
            log.error("Failed to read options.yaml", e);
            return null;
        }
    }

    @Override
    public String render(String pageName) {
        return String.format(RENDER_TEMPLATE, getActivatedThemeId(), pageName);
    }

    @Override
    public String getActivatedThemeId() {
        return optionService.getByProperty(PrimaryProperties.THEME).orElse(DEFAULT_THEME_NAME);
    }

    @Override
    public ThemeProperty activeTheme(String themeId) {
        // Check existence of the theme
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        // Save the theme to database
        optionService.saveProperty(PrimaryProperties.THEME, themeId);

        try {
            // TODO Refactor here in the future
            configuration.setSharedVariable("themeName", themeId);
            configuration.setSharedVariable("options", optionService.listOptions());
        } catch (TemplateModelException e) {
            throw new ServiceException("Failed to set shared variable", e).setErrorData(themeId);
        }

        return themeProperty;
    }

    /**
     * Lists theme files as tree view.
     *
     * @param topPath must not be null
     * @return theme file tree view or null only if the top path is not a directory
     */
    @Nullable
    private List<ThemeFile> listThemeFileTree(@NonNull Path topPath) {
        Assert.notNull(topPath, "Top path must not be null");

        // Check file type
        if (!Files.isDirectory(topPath)) {
            return null;
        }

        try {
            List<ThemeFile> themeFiles = new LinkedList<>();

            Files.list(topPath).forEach(path -> {
                // Build theme file
                ThemeFile themeFile = new ThemeFile();
                themeFile.setName(path.getFileName().toString());
                themeFile.setPath(path.toString());
                themeFile.setIsFile(Files.isRegularFile(path));
                themeFile.setEditable(isEditable(path));

                if (Files.isDirectory(path)) {
                    themeFile.setNode(listThemeFileTree(path));
                }

                // Add to theme files
                themeFiles.add(themeFile);
            });

            // Sort with name
            themeFiles.sort(Comparator.comparing(ThemeFile::getName));

            return themeFiles;
        } catch (IOException e) {
            throw new ServiceException("Failed to list sub files", e);
        }
    }

    /**
     * Check if the given path is editable.
     *
     * @param path must not be null
     * @return true if the given path is editable; false otherwise
     */
    private boolean isEditable(@NonNull Path path) {
        Assert.notNull(path, "Path must not be null");

        boolean isEditable = Files.isReadable(path) && Files.isWritable(path);

        if (!isEditable) {
            return false;
        }

        // Check suffix
        for (String suffix : CAN_EDIT_SUFFIX) {
            if (path.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if directory is valid or not.
     *
     * @param absoluteName must not be blank
     * @throws ForbiddenException throws when the given absolute directory name is invalid
     */
    private void checkDirectory(@NonNull String absoluteName) {
        Assert.hasText(absoluteName, "Absolute name must not be blank");

        ThemeProperty activeThemeProperty = getThemeOfNonNullBy(getActivatedThemeId());

        boolean valid = Paths.get(absoluteName).startsWith(activeThemeProperty.getThemePath());

        if (!valid) {
            throw new ForbiddenException("You cannot access " + absoluteName).setErrorData(absoluteName);
        }
    }

    /**
     * Gets theme property.
     *
     * @param themePath must not be null
     * @return theme property
     */
    private ThemeProperty getProperty(@NonNull Path themePath) {
        Assert.notNull(themePath, "Theme path must not be null");

        Path propertyPath = themePath.resolve(THEME_PROPERTY_FILE_NAME);
        if (!Files.exists(propertyPath)) {
            return null;
        }

        try {
            Properties properties = new Properties();
            // Load properties
            properties.load(new InputStreamReader(Files.newInputStream(propertyPath), StandardCharsets.UTF_8));

            ThemeProperty themeProperty = new ThemeProperty();
            themeProperty.setId(properties.getProperty("theme.id"));
            themeProperty.setName(properties.getProperty("theme.name"));
            themeProperty.setWebsite(properties.getProperty("theme.website"));
            themeProperty.setDescription(properties.getProperty("theme.description"));
            themeProperty.setLogo(properties.getProperty("theme.logo"));
            themeProperty.setVersion(properties.getProperty("theme.version"));
            themeProperty.setAuthor(properties.getProperty("theme.author"));
            themeProperty.setAuthorWebsite(properties.getProperty("theme.author.website"));
            themeProperty.setThemePath(themePath.toString());
            themeProperty.setHasOptions(hasOptions(themePath));

            return themeProperty;
        } catch (IOException e) {
            // TODO Consider to ignore this error, then return null
            throw new ServiceException("Failed to load: " + themePath.toString(), e);
        }
    }

    /**
     * Check existence of the options.
     *
     * @param themePath theme path must not be null
     * @return true if it has options; false otherwise
     */
    private boolean hasOptions(@NonNull Path themePath) {
        Assert.notNull(themePath, "Path must not be null");

        for (String optionsName : OPTIONS_NAMES) {
            // Resolve the options path
            Path optionsPath = themePath.resolve(optionsName);

            log.debug("Check options file for path: [{}]", optionsPath);

            if (Files.exists(optionsPath)) {
                return true;
            }
        }

        return false;
    }
}
