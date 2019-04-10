package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.entity.ThemeSetting;
import run.halo.app.repository.ThemeSettingRepository;
import run.halo.app.service.ThemeSettingService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

import java.util.List;
import java.util.Map;

/**
 * Theme setting service implementation.
 *
 * @author johnniang
 * @date 4/8/19
 */
@Slf4j
@Service
public class ThemeSettingServiceImpl extends AbstractCrudService<ThemeSetting, Integer> implements ThemeSettingService {

    private final ThemeSettingRepository themeSettingRepository;

    public ThemeSettingServiceImpl(ThemeSettingRepository themeSettingRepository) {
        super(themeSettingRepository);
        this.themeSettingRepository = themeSettingRepository;
    }

    @Override
    public ThemeSetting save(String key, String value, String themeId) {
        Assert.notNull(key, "Setting key must not be null");
        assertThemeIdHasText(themeId);

        if (StringUtils.isBlank(value)) {
            return themeSettingRepository.findByThemeIdAndKey(themeId, key)
                    .map(setting -> {
                        themeSettingRepository.delete(setting);
                        log.debug("Removed theme setting: [{}]", setting);
                        return setting;
                    }).orElse(null);
        }

        ThemeSetting themeSetting = themeSettingRepository.findByThemeIdAndKey(themeId, key)
                .map(setting -> {
                    setting.setValue(value);
                    return setting;
                }).orElseGet(() -> {
                    ThemeSetting setting = new ThemeSetting();
                    setting.setKey(key);
                    setting.setValue(value);
                    setting.setThemeId(themeId);
                    return setting;
                });

        // Save the theme setting
        return themeSettingRepository.save(themeSetting);
    }

    @Override
    public void save(Map<String, Object> settings, String themeId) {
        assertThemeIdHasText(themeId);

        if (CollectionUtils.isEmpty(settings)) {
            return;
        }

        // Save the settings
        settings.forEach((key, value) -> save(key, value.toString(), themeId));
    }

    @Override
    public List<ThemeSetting> listBy(String themeId) {
        assertThemeIdHasText(themeId);

        return themeSettingRepository.findAllByThemeId(themeId);
    }

    @Override
    public Map<String, Object> listAsMapBy(String themeId) {

        // TODO Convert to corresponding data type
        return ServiceUtils.convertToMap(listBy(themeId), ThemeSetting::getKey, ThemeSetting::getValue);
    }

    /**
     * Asserts theme id has text.
     *
     * @param themeId theme id to be checked
     */
    private void assertThemeIdHasText(String themeId) {
        Assert.hasText(themeId, "Theme id must not be null");
    }
}
