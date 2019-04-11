package run.halo.app.service.impl;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.*;
import run.halo.app.handler.theme.ThemeConfigResolver;
import run.halo.app.handler.theme.ThemePropertyResolver;
import run.halo.app.handler.theme.support.Group;
import run.halo.app.handler.theme.support.ThemeProperty;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
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

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    private final Configuration configuration;

    private final ThemeConfigResolver themeConfigResolver;

    private final ThemePropertyResolver themePropertyResolver;
    /**
     * Activated theme id.
     */
    private String activatedThemeId;

    public ThemeServiceImpl(HaloProperties haloProperties,
                            OptionService optionService,
                            StringCacheStore cacheStore,
                            Configuration configuration,
                            ThemeConfigResolver themeConfigResolver,
                            ThemePropertyResolver themePropertyResolver) {
        this.optionService = optionService;
        this.cacheStore = cacheStore;
        this.configuration = configuration;
        this.themeConfigResolver = themeConfigResolver;
        workDir = Paths.get(haloProperties.getWorkDir(), THEME_FOLDER);
        this.themePropertyResolver = themePropertyResolver;
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

        return themes.stream().filter(themeProperty -> StringUtils.equals(themeProperty.getId(), themeId)).findFirst();
    }

    @Override
    public List<ThemeProperty> getThemes() {
        // Fetch themes from cache
        List<ThemeProperty> result = cacheStore.get(THEMES_CACHE_KEY).map(themesCache -> {
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

        return CollectionUtils.isEmpty(result) ? Collections.emptyList() : result;
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
        List<String> templates = new ArrayList<>();
        File themePath = new File(getThemeBasePath(), themeId);
        File[] themeFiles = themePath.listFiles();
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

        if (themeId.equals(getActivatedThemeId())) {
            // Prevent to delete the activated theme
            throw new BadRequestException("You can't delete the activated theme").setErrorData(themeId);
        }

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
    public List<Group> fetchConfig(String themeId) {
        Assert.hasText(themeId, "Theme name must not be blank");

        // Get theme property
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        if (!themeProperty.isHasOptions()) {
            // If this theme dose not has an option, then return empty list
            return Collections.emptyList();
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

                // Read the yaml file
                String optionContent = new String(Files.readAllBytes(optionsPath));

                // Resolve it
                return themeConfigResolver.resolve(optionContent);
            }

            return Collections.emptyList();
        } catch (IOException e) {
            throw new ServiceException("Failed to read options file", e);
        }
    }

    @Override
    public String render(String pageName) {
        return String.format(RENDER_TEMPLATE, getActivatedThemeId(), pageName);
    }

    @Override
    public String getActivatedThemeId() {
        if (StringUtils.isBlank(activatedThemeId)) {
            synchronized (this) {
                if (StringUtils.isBlank(activatedThemeId)) {
                    return optionService.getByProperty(PrimaryProperties.THEME).orElse(DEFAULT_THEME_NAME);
                }
            }
        }

        return activatedThemeId;
    }

    /**
     * Set activated theme id.
     *
     * @param themeId theme id
     */
    private void setActivatedThemeId(@Nullable String themeId) {
        this.activatedThemeId = themeId;
    }

    @Override
    public ThemeProperty activeTheme(String themeId) {
        // Check existence of the theme
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        // Save the theme to database
        optionService.saveProperty(PrimaryProperties.THEME, themeId);


        // Set the activated theme id
        setActivatedThemeId(themeId);

        // Clear the cache
        cacheStore.delete(THEMES_CACHE_KEY);

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

            // Sort with isFile param
            themeFiles.sort(new ThemeFile());

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

    @Nullable
    private Path getPropertyPath(@NonNull Path themePath) {
        Assert.notNull(themePath, "Theme path must not be null");

        for (String propertyPathName : THEME_PROPERTY_FILE_NAMES) {
            Path propertyPath = themePath.resolve(propertyPathName);

            log.debug("Attempting to find property file: [{}]", propertyPath);
            if (Files.exists(propertyPath) && Files.isReadable(propertyPath)) {
                log.debug("Found property file: [{}]", propertyPath);
                return propertyPath;
            }
        }

        return null;
    }

    /**
     * Gets theme property.
     *
     * @param themePath must not be null
     * @return theme property
     */
    @NonNull
    private ThemeProperty getProperty(@NonNull Path themePath) {
        Assert.notNull(themePath, "Theme path must not be null");

        Path propertyPath = getPropertyPath(themePath);

        if (propertyPath == null) {
            throw new ThemePropertyMissingException(themePath + " dose not exist any theme property file").setErrorData(themePath);
        }

        try {
            // Get property content
            String propertyContent = new String(Files.readAllBytes(propertyPath));

            // Resolve the base properties
            ThemeProperty themeProperty = themePropertyResolver.resolve(propertyContent);

            // Resolve additional properties
            themeProperty.setThemePath(themePath.toString());
            themeProperty.setHasOptions(hasOptions(themePath));
            themeProperty.setActivated(false);

            // Set screenshots
            getScreenshotsFileName(themePath).ifPresent(screenshotsName ->
                    themeProperty.setScreenshots(StringUtils.join(optionService.getBlogBaseUrl(),
                            "/",
                            FilenameUtils.getBasename(themeProperty.getThemePath()),
                            "/",
                            screenshotsName)));

            if (StringUtils.equals(themeProperty.getId(), getActivatedThemeId())) {
                // Set activation
                themeProperty.setActivated(true);
            }

            return themeProperty;
        } catch (IOException e) {
            throw new ThemePropertyMissingException("Cannot resolve theme property", e).setErrorData(propertyPath.toString());
        }
    }

    /**
     * Gets screenshots file name.
     *
     * @param themePath theme path must not be null
     * @return screenshots file name or null if the given theme path has not screenshots
     * @throws IOException throws when listing files
     */
    private Optional<String> getScreenshotsFileName(@NonNull Path themePath) throws IOException {
        Assert.notNull(themePath, "Theme path must not be null");

        return Files.list(themePath)
                .filter(path -> Files.isRegularFile(path)
                        && Files.isReadable(path)
                        && FilenameUtils.getBasename(path.toString()).equalsIgnoreCase(THEME_SCREENSHOTS_NAME))
                .findFirst()
                .map(path -> path.getFileName().toString());
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
