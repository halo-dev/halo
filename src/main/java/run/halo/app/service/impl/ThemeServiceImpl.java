package run.halo.app.service.impl;

import cn.hutool.core.io.FileUtil;
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
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.*;
import run.halo.app.handler.theme.config.ThemeConfigResolver;
import run.halo.app.handler.theme.config.ThemePropertyResolver;
import run.halo.app.handler.theme.config.support.Group;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.support.HaloMediaType;
import run.halo.app.utils.FileUtils;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import static run.halo.app.model.support.HaloConst.DEFAULT_THEME_ID;

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

        Set<ThemeProperty> themes = getThemes();

        log.debug("Themes type: [{}]", themes.getClass());
        log.debug("Themes: [{}]", themes);

        return themes.stream().filter(themeProperty -> StringUtils.equals(themeProperty.getId(), themeId)).findFirst();
    }

    @Override
    public Set<ThemeProperty> getThemes() {
        Optional<String> themeCacheString = cacheStore.get(THEMES_CACHE_KEY);

        try {
            if (themeCacheString.isPresent()) {
                // Convert to theme properties
                ThemeProperty[] themeProperties = JsonUtils.jsonToObject(themeCacheString.get(), ThemeProperty[].class);
                return new HashSet<>(Arrays.asList(themeProperties));
            }

            // List and filter sub folders
            List<Path> themePaths = Files.list(getBasePath()).filter(path -> Files.isDirectory(path)).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(themePaths)) {
                return Collections.emptySet();
            }

            // Get theme properties
            Set<ThemeProperty> themes = themePaths.stream().map(this::getProperty).collect(Collectors.toSet());

            // Cache the themes
            cacheStore.put(THEMES_CACHE_KEY, JsonUtils.objectToJson(themes));

            return themes;
        } catch (IOException e) {
            throw new ServiceException("Failed to get themes", e);
        }
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
            FileUtil.del(Paths.get(themeProperty.getThemePath()));

            // Delete theme cache
            clearThemeCache();
        } catch (Exception e) {
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
                    return optionService.getByProperty(PrimaryProperties.THEME).orElse(DEFAULT_THEME_ID);
                }
            }
        }

        return activatedThemeId;
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
        clearThemeCache();

        try {
            // TODO Refactor here in the future
            configuration.setSharedVariable("themeName", themeId);
            configuration.setSharedVariable("options", optionService.listOptions());
        } catch (TemplateModelException e) {
            throw new ServiceException("Failed to set shared variable", e).setErrorData(themeId);
        }

        return themeProperty;
    }

    @Override
    public ThemeProperty upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        if (!HaloMediaType.isZipType(file.getContentType())) {
            throw new UnsupportedMediaTypeException("Unsupported theme media type: " + file.getContentType()).setErrorData(file.getOriginalFilename());
        }

        ZipInputStream zis = null;
        Path tempPath = null;

        try {
            // Create temp directory
            tempPath = Files.createTempDirectory("halo");
            String basename = FilenameUtils.getBasename(file.getOriginalFilename());
            Path themeTempPath = tempPath.resolve(basename);

            // Check directory traversal
            FileUtils.checkDirectoryTraversal(tempPath, themeTempPath);

            // New zip input stream
            zis = new ZipInputStream(file.getInputStream());

            // Unzip to temp path
            FileUtils.unzip(zis, themeTempPath);

            // Go to the base folder

            // Add the theme to system
            return add(FileUtils.skipZipParentFolder(themeTempPath));
        } catch (IOException e) {
            throw new ServiceException("Failed to upload theme file: " + file.getOriginalFilename(), e);
        } finally {
            // Close zip input stream
            FileUtils.closeQuietly(zis);
            // Delete folder after testing
            FileUtils.deleteFolderQuietly(tempPath);
        }
    }

    @Override
    public ThemeProperty add(Path themeTmpPath) throws IOException {
        Assert.notNull(themeTmpPath, "Theme temporary path must not be null");
        Assert.isTrue(Files.isDirectory(themeTmpPath), "Theme temporary path must be a directory");

        // Check property config
        ThemeProperty tmpThemeProperty = getProperty(themeTmpPath);

        // Check theme existence
        boolean isExist = getThemes().stream()
                .anyMatch(themeProperty -> themeProperty.getId().equalsIgnoreCase(tmpThemeProperty.getId()));

        if (isExist) {
            throw new AlreadyExistsException("The theme with id " + tmpThemeProperty.getId() + " has already existed");
        }

        // Copy the temporary path to current theme folder
        Path targetThemePath = workDir.resolve(tmpThemeProperty.getId());
        FileUtils.copyFolder(themeTmpPath, targetThemePath);

        // Get property again
        ThemeProperty property = getProperty(targetThemePath);

        // Clear theme cache
        clearThemeCache();

        // Delete cache
        return property;
    }

    /**
     * Clears theme cache.
     */
    private void clearThemeCache() {
        cacheStore.delete(THEMES_CACHE_KEY);
    }

    /**
     * Set activated theme id.
     *
     * @param themeId theme id
     */
    private void setActivatedThemeId(@Nullable String themeId) {
        this.activatedThemeId = themeId;
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
        ThemeProperty activeThemeProperty = getThemeOfNonNullBy(getActivatedThemeId());
        FileUtils.checkDirectoryTraversal(activeThemeProperty.getThemePath(), absoluteName);
    }

    /**
     * Gets property path of nullable.
     *
     * @param themePath theme path.
     * @return an optional property path
     */
    @NonNull
    private Optional<Path> getThemePropertyPathOfNullable(@NonNull Path themePath) {
        Assert.notNull(themePath, "Theme path must not be null");

        for (String propertyPathName : THEME_PROPERTY_FILE_NAMES) {
            Path propertyPath = themePath.resolve(propertyPathName);

            log.debug("Attempting to find property file: [{}]", propertyPath);
            if (Files.exists(propertyPath) && Files.isReadable(propertyPath)) {
                log.debug("Found property file: [{}]", propertyPath);
                return Optional.of(propertyPath);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets property path of non null.
     *
     * @param themePath theme path.
     * @return property path won't be null
     */
    @NonNull
    private Path getThemePropertyPath(@NonNull Path themePath) {
        return getThemePropertyPathOfNullable(themePath).orElseThrow(() -> new ThemePropertyMissingException(themePath + " dose not exist any theme property file").setErrorData(themePath));
    }

    private Optional<ThemeProperty> getPropertyOfNullable(Path themePath) {
        Assert.notNull(themePath, "Theme path must not be null");

        Path propertyPath = getThemePropertyPath(themePath);

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

            return Optional.of(themeProperty);

        } catch (IOException e) {
            log.error("Failed to load theme property file", e);
        }

        return Optional.empty();
    }

    /**
     * Gets theme property.
     *
     * @param themePath must not be null
     * @return theme property
     */
    @NonNull
    private ThemeProperty getProperty(@NonNull Path themePath) {
        return getPropertyOfNullable(themePath).orElseThrow(() -> new ThemePropertyMissingException("Cannot resolve theme property").setErrorData(themePath));
    }

    /**
     * Gets screenshots file name.
     *
     * @param themePath theme path must not be null
     * @return screenshots file name or null if the given theme path has not screenshots
     * @throws IOException throws when listing files
     */
    @NonNull
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
