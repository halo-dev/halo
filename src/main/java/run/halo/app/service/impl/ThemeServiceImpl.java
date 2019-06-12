package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.event.theme.ThemeActivatedEvent;
import run.halo.app.event.theme.ThemeUpdatedEvent;
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
import run.halo.app.utils.FileUtils;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.GitUtils;
import run.halo.app.utils.HaloUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static run.halo.app.model.support.HaloConst.DEFAULT_THEME_ID;

/**
 * Theme service implementation.
 *
 * @author ryanwang
 * @date : 2019/3/26
 */
@Slf4j
@Service
public class ThemeServiceImpl implements ThemeService {

    /**
     * Theme work directory.
     */
    private final Path themeWorkDir;

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    private final ThemeConfigResolver themeConfigResolver;

    private final ThemePropertyResolver themePropertyResolver;

    private final RestTemplate restTemplate;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Activated theme id.
     */
    private String activatedThemeId;

    /**
     * Activated theme property.
     */
    private ThemeProperty activatedTheme;

    public ThemeServiceImpl(HaloProperties haloProperties,
                            OptionService optionService,
                            StringCacheStore cacheStore,
                            ThemeConfigResolver themeConfigResolver,
                            ThemePropertyResolver themePropertyResolver,
                            RestTemplate restTemplate,
                            ApplicationEventPublisher eventPublisher) {
        this.optionService = optionService;
        this.cacheStore = cacheStore;
        this.themeConfigResolver = themeConfigResolver;
        this.themePropertyResolver = themePropertyResolver;
        this.restTemplate = restTemplate;

        themeWorkDir = Paths.get(haloProperties.getWorkDir(), THEME_FOLDER);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ThemeProperty getThemeOfNonNullBy(String themeId) {
        return getThemeBy(themeId).orElseThrow(() -> new NotFoundException("Theme with id: " + themeId + " was not found").setErrorData(themeId));
    }

    @Override
    public Optional<ThemeProperty> getThemeBy(String themeId) {
        if (StringUtils.isBlank(themeId)) {
            return Optional.empty();
        }

        // Get all themes
        Set<ThemeProperty> themes = getThemes();

        // filter and find first
        return themes.stream().filter(themeProperty -> StringUtils.equals(themeProperty.getId(), themeId)).findFirst();
    }

    @Override
    public Set<ThemeProperty> getThemes() {

        Optional<ThemeProperty[]> themePropertiesOptional = cacheStore.getAny(THEMES_CACHE_KEY, ThemeProperty[].class);

        if (themePropertiesOptional.isPresent()) {
            // Convert to theme properties
            ThemeProperty[] themeProperties = themePropertiesOptional.get();
            return new HashSet<>(Arrays.asList(themeProperties));
        }

        try (Stream<Path> pathStream = Files.list(getBasePath())) {

            // List and filter sub folders
            List<Path> themePaths = pathStream.filter(path -> Files.isDirectory(path))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(themePaths)) {
                return Collections.emptySet();
            }

            // Get theme properties
            Set<ThemeProperty> themes = themePaths.stream().map(this::getProperty).collect(Collectors.toSet());

            // Cache the themes
            cacheStore.putAny(THEMES_CACHE_KEY, themes);

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
    public Set<String> listCustomTemplates(String themeId) {
        // Get the theme path
        Path themePath = Paths.get(getThemeOfNonNullBy(themeId).getThemePath());

        try (Stream<Path> pathStream = Files.list(themePath)) {
            return pathStream.filter(path -> StringUtils.startsWithIgnoreCase(path.getFileName().toString(), CUSTOM_SHEET_PREFIX))
                    .map(path -> {
                        // Remove prefix
                        String customTemplate = StringUtils.removeStartIgnoreCase(path.getFileName().toString(), CUSTOM_SHEET_PREFIX);
                        // Remove suffix
                        return StringUtils.removeEndIgnoreCase(customTemplate, HaloConst.SUFFIX_FTL);
                    })
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new ServiceException("Failed to list files of path " + themePath.toString(), e);
        }
    }

    @Override
    public boolean templateExists(String template) {
        if (StringUtils.isBlank(template)) {
            return false;
        }

        // Resolve template path
        Path templatePath = Paths.get(getActivatedTheme().getThemePath(), template);

        // Check the directory
        checkDirectory(templatePath.toString());

        // Check existence
        return Files.exists(templatePath);
    }

    @Override
    public boolean themeExists(String themeId) {
        return getThemeBy(themeId).isPresent();
    }

    @Override
    public Path getBasePath() {
        return themeWorkDir;
    }

    @Override
    public String getTemplateContent(String absolutePath) {
        // Check the path
        checkDirectory(absolutePath);

        // Read file
        Path path = Paths.get(absolutePath);
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ServiceException("读取模板内容失败 " + absolutePath, e);
        }
    }

    @Override
    public void saveTemplateContent(String absolutePath, String content) {
        // Check the path
        checkDirectory(absolutePath);

        // Write file
        Path path = Paths.get(absolutePath);
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ServiceException("保存模板内容失败 " + absolutePath, e);
        }
    }

    @Override
    public void deleteTheme(String themeId) {
        // Get the theme property
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        if (themeId.equals(getActivatedThemeId())) {
            // Prevent to delete the activated theme
            throw new BadRequestException("不能删除正在使用的主题").setErrorData(themeId);
        }

        try {
            // Delete the folder
            FileUtils.deleteFolder(Paths.get(themeProperty.getThemePath()));
            // Delete theme cache
            eventPublisher.publishEvent(new ThemeUpdatedEvent(this));
        } catch (Exception e) {
            throw new ServiceException("主题删除失败", e).setErrorData(themeId);
        }
    }

    @Override
    public List<Group> fetchConfig(String themeId) {
        Assert.hasText(themeId, "Theme id must not be blank");

        // Get theme property
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        if (!themeProperty.isHasOptions()) {
            // If this theme dose not has an option, then return empty list
            return Collections.emptyList();
        }

        try {
            for (String optionsName : SETTINGS_NAMES) {
                // Resolve the options path
                Path optionsPath = Paths.get(themeProperty.getThemePath(), optionsName);

                log.debug("Finding options in: [{}]", optionsPath.toString());

                // Check existence
                if (!Files.exists(optionsPath)) {
                    continue;
                }

                // Read the yaml file
                String optionContent = new String(Files.readAllBytes(optionsPath), StandardCharsets.UTF_8);

                // Resolve it
                return themeConfigResolver.resolve(optionContent);
            }

            return Collections.emptyList();
        } catch (IOException e) {
            throw new ServiceException("读取主题配置文件失败", e);
        }
    }

    @Override
    public String render(String pageName) {
        // Get activated theme
        ThemeProperty activatedTheme = getActivatedTheme();
        // Build render url
        return String.format(RENDER_TEMPLATE, activatedTheme.getFolderName(), pageName);
    }

    @Override
    public String getActivatedThemeId() {
        if (activatedThemeId == null) {
            synchronized (this) {
                if (activatedThemeId == null) {
                    activatedThemeId = optionService.getByPropertyOrDefault(PrimaryProperties.THEME, String.class, DEFAULT_THEME_ID);
                }
            }
        }

        return activatedThemeId;
    }

    @Override
    public ThemeProperty getActivatedTheme() {
        if (activatedTheme == null) {
            synchronized (this) {
                if (activatedTheme == null) {
                    // Get theme property
                    activatedTheme = getThemeOfNonNullBy(getActivatedThemeId());
                }
            }
        }

        return activatedTheme;
    }

    /**
     * Sets activated theme.
     *
     * @param activatedTheme activated theme
     */
    private void setActivatedTheme(@Nullable ThemeProperty activatedTheme) {
        this.activatedTheme = activatedTheme;
        this.activatedThemeId = Optional.ofNullable(activatedTheme).map(ThemeProperty::getId).orElse(null);
    }

    @Override
    public ThemeProperty activateTheme(String themeId) {
        // Check existence of the theme
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);

        // Save the theme to database
        optionService.saveProperty(PrimaryProperties.THEME, themeId);

        // Set activated theme
        setActivatedTheme(themeProperty);

        // Clear the cache
        eventPublisher.publishEvent(new ThemeUpdatedEvent(this));

        // Publish a theme activated event
        eventPublisher.publishEvent(new ThemeActivatedEvent(this));

        return themeProperty;
    }

    @Override
    public ThemeProperty upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        if (!StringUtils.endsWithIgnoreCase(file.getOriginalFilename(), ".zip")) {
            throw new UnsupportedMediaTypeException("不支持的文件类型: " + file.getContentType()).setErrorData(file.getOriginalFilename());
        }

        ZipInputStream zis = null;
        Path tempPath = null;

        try {
            // Create temp directory
            tempPath = FileUtils.createTempDirectory();
            String basename = FilenameUtils.getBasename(file.getOriginalFilename());
            Path themeTempPath = tempPath.resolve(basename);

            // Check directory traversal
            FileUtils.checkDirectoryTraversal(tempPath, themeTempPath);

            // New zip input stream
            zis = new ZipInputStream(file.getInputStream());

            // Unzip to temp path
            FileUtils.unzip(zis, themeTempPath);

            // Go to the base folder and add the theme into system
            return add(FileUtils.tryToSkipZipParentFolder(themeTempPath));
        } catch (IOException e) {
            throw new ServiceException("上传主题失败: " + file.getOriginalFilename(), e);
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

        log.debug("Children path of [{}]:", themeTmpPath);

        try (Stream<Path> pathStream = Files.list(themeTmpPath)) {
            pathStream.forEach(path -> log.debug(path.toString()));
        }

        // Check property config
        ThemeProperty tmpThemeProperty = getProperty(themeTmpPath);

        // Check theme existence
        boolean isExist = getThemes().stream()
                .anyMatch(themeProperty -> themeProperty.getId().equalsIgnoreCase(tmpThemeProperty.getId()));

        if (isExist) {
            throw new AlreadyExistsException("当前安装的主题已存在");
        }

        // Copy the temporary path to current theme folder
        Path targetThemePath = themeWorkDir.resolve(tmpThemeProperty.getId());
        FileUtils.copyFolder(themeTmpPath, targetThemePath);

        // Get property again
        ThemeProperty property = getProperty(targetThemePath);

        // Clear theme cache
        this.eventPublisher.publishEvent(new ThemeUpdatedEvent(this));

        // Delete cache
        return property;
    }

    @Override
    public ThemeProperty fetch(String uri) {
        Assert.hasText(uri, "Theme remote uri must not be blank");

        Path tmpPath = null;

        try {
            // Create temp path
            tmpPath = FileUtils.createTempDirectory();
            // Create temp path
            Path themeTmpPath = tmpPath.resolve(HaloUtils.randomUUIDWithoutDash());

            if (StringUtils.endsWithIgnoreCase(uri, ".zip")) {
                downloadZipAndUnzip(uri, themeTmpPath);
            } else {
                uri = StringUtils.appendIfMissingIgnoreCase(uri, ".git", ".git");
                // Clone from git
                GitUtils.cloneFromGit(uri, themeTmpPath);
            }

            return add(themeTmpPath);
        } catch (IOException | GitAPIException e) {
            throw new ServiceException("主题拉取失败 " + uri, e);
        } finally {
            FileUtils.deleteFolderQuietly(tmpPath);
        }
    }

    @Override
    public void reload() {
        eventPublisher.publishEvent(new ThemeUpdatedEvent(this));
    }

    @Override
    public ThemeProperty update(String themeId) {
        Assert.hasText(themeId, "Theme id must not be blank");

        ThemeProperty updatingTheme = getThemeOfNonNullBy(themeId);

        try {
            pullFromGit(updatingTheme);
        } catch (Exception e) {
            throw new ThemeUpdateException("主题更新失败！您与主题作者可能同时更改了同一个文件，您也可以尝试删除主题并重新拉取最新的主题", e).setErrorData(themeId);
        }

        eventPublisher.publishEvent(new ThemeUpdatedEvent(this));

        return getThemeOfNonNullBy(themeId);
    }

    private void pullFromGit(@NonNull ThemeProperty themeProperty) throws IOException, GitAPIException, URISyntaxException {
        Assert.notNull(themeProperty, "Theme property must not be null");

        // Get branch
        String branch = StringUtils.isBlank(themeProperty.getBranch()) ?
                DEFAULT_REMOTE_BRANCH : themeProperty.getBranch();

        Git git = null;

        try {
            git = GitUtils.openOrInit(Paths.get(themeProperty.getThemePath()));
            // Force to set remote name
            git.remoteRemove().setRemoteName(THEME_PROVIDER_REMOTE_NAME).call();
            RemoteConfig remoteConfig = git.remoteAdd()
                    .setName(THEME_PROVIDER_REMOTE_NAME)
                    .setUri(new URIish(themeProperty.getRepo()))
                    .call();

            // Add all changes
            git.add()
                    .addFilepattern(".")
                    .call();
            // Commit the changes
            git.commit().setMessage("Commit by halo automatically").call();

            // Check out to specified branch
            if (!StringUtils.equalsIgnoreCase(branch, git.getRepository().getBranch())) {
                boolean present = git.branchList()
                        .call()
                        .stream()
                        .map(Ref::getName)
                        .anyMatch(name -> StringUtils.equalsIgnoreCase(name, branch));

                git.checkout()
                        .setCreateBranch(true)
                        .setForced(!present)
                        .setName(branch)
                        .call();
            }

            // Pull with rebasing
            PullResult pullResult = git.pull()
                    .setRemote(remoteConfig.getName())
                    .setRemoteBranchName(branch)
                    .setRebase(true)
                    .call();

            if (!pullResult.isSuccessful()) {
                log.debug("Rebase result: [{}]", pullResult.getRebaseResult());
                log.debug("Merge result: [{}]", pullResult.getMergeResult());

                throw new ThemeUpdateException("拉取失败！您与主题作者可能同时更改了同一个文件");
            }
        } finally {
            GitUtils.closeQuietly(git);
        }

    }

    /**
     * Downloads zip file and unzip it into specified path.
     *
     * @param zipUrl     zip url must not be null
     * @param targetPath target path must not be null
     * @throws IOException throws when download zip or unzip error
     */
    private void downloadZipAndUnzip(@NonNull String zipUrl, @NonNull Path targetPath) throws IOException {
        Assert.hasText(zipUrl, "Zip url must not be blank");

        log.debug("Downloading [{}]", zipUrl);
        // Download it
        ResponseEntity<byte[]> downloadResponse = restTemplate.getForEntity(zipUrl, byte[].class);

        log.debug("Download response: [{}]", downloadResponse.getStatusCode());

        if (downloadResponse.getStatusCode().isError() || downloadResponse.getBody() == null) {
            throw new ServiceException("下载失败 " + zipUrl + ", 状态码: " + downloadResponse.getStatusCode());
        }

        log.debug("Downloaded [{}]", zipUrl);

        // Unzip it
        FileUtils.unzip(downloadResponse.getBody(), targetPath);
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

        try (Stream<Path> pathStream = Files.list(topPath)) {
            List<ThemeFile> themeFiles = new LinkedList<>();

            pathStream.forEach(path -> {
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
            if (path.toString().endsWith(suffix)) {
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

        log.warn("Property file was not found in [{}]", themePath);

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
        return getThemePropertyPathOfNullable(themePath).orElseThrow(() -> new ThemePropertyMissingException(themePath + " 没有说明文件").setErrorData(themePath));
    }

    private Optional<ThemeProperty> getPropertyOfNullable(Path themePath) {
        Assert.notNull(themePath, "Theme path must not be null");

        Path propertyPath = getThemePropertyPath(themePath);

        try {
            // Get property content
            String propertyContent = new String(Files.readAllBytes(propertyPath), StandardCharsets.UTF_8);

            // Resolve the base properties
            ThemeProperty themeProperty = themePropertyResolver.resolve(propertyContent);

            // Resolve additional properties
            themeProperty.setThemePath(themePath.toString());
            themeProperty.setFolderName(themePath.getFileName().toString());
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
        return getPropertyOfNullable(themePath).orElseThrow(() -> new ThemePropertyMissingException("该主题没有说明文件").setErrorData(themePath));
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

        try (Stream<Path> pathStream = Files.list(themePath)) {
            return pathStream.filter(path -> Files.isRegularFile(path)
                    && Files.isReadable(path)
                    && FilenameUtils.getBasename(path.toString()).equalsIgnoreCase(THEME_SCREENSHOTS_NAME))
                    .findFirst()
                    .map(path -> path.getFileName().toString());
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

        for (String optionsName : SETTINGS_NAMES) {
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
