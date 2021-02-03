package run.halo.app.repository;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static run.halo.app.utils.FileUtils.copyFolder;
import static run.halo.app.utils.FileUtils.deleteFolderQuietly;
import static run.halo.app.utils.VersionUtil.compareVersion;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.event.options.OptionUpdatedEvent;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.ServiceException;
import run.halo.app.exception.ThemeNotSupportException;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.entity.Option;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.theme.ThemePropertyScanner;
import run.halo.app.utils.FileUtils;

/**
 * Theme repository implementation.
 *
 * @author johnniang
 */
@Repository
@Slf4j
public class ThemeRepositoryImpl implements ThemeRepository {

    private final OptionRepository optionRepository;

    private final HaloProperties properties;

    private final ApplicationEventPublisher eventPublisher;

    public ThemeRepositoryImpl(OptionRepository optionRepository,
        HaloProperties properties,
        ApplicationEventPublisher eventPublisher) {
        this.optionRepository = optionRepository;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String getActivatedThemeId() {
        return optionRepository.findByKey(PrimaryProperties.THEME.getValue())
            .map(Option::getValue)
            .orElse(HaloConst.DEFAULT_THEME_ID);
    }

    @Override
    public ThemeProperty getActivatedThemeProperty() {
        return fetchThemePropertyByThemeId(getActivatedThemeId()).orElseThrow();
    }

    @Override
    public Optional<ThemeProperty> fetchThemePropertyByThemeId(String themeId) {
        return listAll().stream()
            .filter(property -> Objects.equals(themeId, property.getId()))
            .findFirst();
    }

    @Override
    public List<ThemeProperty> listAll() {
        return ThemePropertyScanner.INSTANCE.scan(getThemeRootPath(), getActivatedThemeId());
    }

    @Override
    public void setActivatedTheme(@NonNull String themeId) {
        Assert.hasText(themeId, "Theme id must not be blank");
        final var newThemeOption = optionRepository.findByKey(PrimaryProperties.THEME.getValue())
            .map(themeOption -> {
                // set theme id
                themeOption.setValue(themeId);
                return themeOption;
            })
            .orElseGet(() -> new Option(PrimaryProperties.THEME.getValue(), themeId));
        optionRepository.save(newThemeOption);

        eventPublisher.publishEvent(new OptionUpdatedEvent(this));
    }

    @Override
    public ThemeProperty attemptToAdd(ThemeProperty newProperty) {
        // 1. check existence
        final var alreadyExist = fetchThemePropertyByThemeId(newProperty.getId()).isPresent();
        if (alreadyExist) {
            throw new AlreadyExistsException("当前安装的主题已存在");
        }

        // 2. check version compatibility
        // Not support current halo version.
        if (checkThemePropertyCompatibility(newProperty)) {
            throw new ThemeNotSupportException(
                "当前主题仅支持 Halo " + newProperty.getRequire() + " 及以上的版本");
        }

        // 3. move the temp folder into templates/themes/{theme_id}
        final var sourceThemePath = Paths.get(newProperty.getThemePath());
        final var targetThemePath =
            getThemeRootPath().resolve(newProperty.getId() + "-" + randomAlphabetic(5));

        // 4. clear target theme folder firstly
        deleteFolderQuietly(targetThemePath);

        log.info("Copying new theme({}) from {} to {}",
            newProperty.getId(),
            sourceThemePath,
            targetThemePath);

        try {
            copyFolder(sourceThemePath, targetThemePath);
        } catch (IOException e) {
            // clear data
            deleteFolderQuietly(targetThemePath);
            throw new ServiceException("复制主题文件失败！", e);
        } finally {
            log.info("Clean temporary theme folder {}", sourceThemePath);
            deleteFolderQuietly(sourceThemePath);
        }

        // or else throw should never happen
        return ThemePropertyScanner.INSTANCE.fetchThemeProperty(targetThemePath).orElseThrow();
    }

    @Override
    public void deleteTheme(String themeId) {
        final var themeProperty = fetchThemePropertyByThemeId(themeId)
            .orElseThrow(() -> new NotFoundException("主题 ID 为 " + themeId + " 不存在或已删除！"));
        deleteTheme(themeProperty);
    }

    @Override
    public void deleteTheme(ThemeProperty themeProperty) {
        final var themePath = Paths.get(themeProperty.getThemePath());
        try {
            FileUtils.deleteFolder(themePath);
        } catch (IOException e) {
            throw new ServiceException("Failed to delete theme path: " + themePath, e);
        }
    }

    @Override
    public boolean checkThemePropertyCompatibility(ThemeProperty themeProperty) {
        // check version compatibility
        // Not support current halo version.
        return StringUtils.isNotEmpty(themeProperty.getRequire())
            && !compareVersion(HaloConst.HALO_VERSION, themeProperty.getRequire());
    }

    private Path getThemeRootPath() {
        return Paths.get(properties.getWorkDir()).resolve("templates/themes");
    }
}
