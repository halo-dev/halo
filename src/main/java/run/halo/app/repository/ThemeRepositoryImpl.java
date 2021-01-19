package run.halo.app.repository;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.entity.Option;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.theme.ThemePropertyScanner;

/**
 * Theme repository implementation.
 *
 * @author johnniang
 */
@Repository
public class ThemeRepositoryImpl implements ThemeRepository {

    private final OptionRepository optionRepository;

    private final HaloProperties properties;

    public ThemeRepositoryImpl(OptionRepository optionRepository,
            HaloProperties properties) {
        this.optionRepository = optionRepository;
        this.properties = properties;
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
        final var themesFolder = Paths.get(properties.getWorkDir()).resolve("templates/themes");
        return ThemePropertyScanner.INSTANCE.scan(themesFolder, getActivatedThemeId());
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
    }
}
