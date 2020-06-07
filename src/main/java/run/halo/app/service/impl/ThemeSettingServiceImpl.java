package run.halo.app.service.impl;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.ServiceException;
import run.halo.app.handler.theme.config.support.Group;
import run.halo.app.handler.theme.config.support.Item;
import run.halo.app.model.entity.ThemeSetting;
import run.halo.app.repository.ThemeSettingRepository;
import run.halo.app.service.ThemeService;
import run.halo.app.service.ThemeSettingService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

import java.util.*;

/**
 * Theme setting service implementation.
 *
 * @author johnniang
 * @date 2019-04-08
 */
@Slf4j
@Service
public class ThemeSettingServiceImpl extends AbstractCrudService<ThemeSetting, Integer> implements ThemeSettingService {

    private final ThemeSettingRepository themeSettingRepository;

    private final ThemeService themeService;

    private final Configuration configuration;

    public ThemeSettingServiceImpl(ThemeSettingRepository themeSettingRepository,
                                   ThemeService themeService,
                                   Configuration configuration) {
        super(themeSettingRepository);
        this.themeSettingRepository = themeSettingRepository;
        this.themeService = themeService;
        this.configuration = configuration;
    }

    @Override
    public ThemeSetting save(String key, String value, String themeId) {
        Assert.notNull(key, "Setting key must not be null");
        assertThemeIdHasText(themeId);

        log.debug("Starting saving theme setting key: [{}], value: [{}]", key, value);

        // Find setting by key
        Optional<ThemeSetting> themeSettingOptional = themeSettingRepository.findByThemeIdAndKey(themeId, key);

        if (StringUtils.isBlank(value)) {
            // Delete it
            return themeSettingOptional
                .map(setting -> {
                    themeSettingRepository.delete(setting);
                    log.debug("Removed theme setting: [{}]", setting);
                    return setting;
                }).orElse(null);
        }

        // Get config item map
        Map<String, Item> itemMap = getConfigItemMap(themeId);

        // Get item info
        Item item = itemMap.get(key);

        // Update or create
        ThemeSetting themeSetting = themeSettingOptional
            .map(setting -> {
                log.debug("Updating theme setting: [{}]", setting);
                setting.setValue(value);
                log.debug("Updated theme setting: [{}]", setting);
                return setting;
            }).orElseGet(() -> {
                ThemeSetting setting = new ThemeSetting();
                setting.setKey(key);
                setting.setValue(value);
                setting.setThemeId(themeId);
                log.debug("Creating theme setting: [{}]", setting);
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

        try {
            configuration.setSharedVariable("settings", listAsMapBy(themeService.getActivatedThemeId()));
        } catch (TemplateModelException e) {
            throw new ServiceException("主题设置保存失败", e);
        }
    }

    @Override
    public List<ThemeSetting> listBy(String themeId) {
        assertThemeIdHasText(themeId);

        return themeSettingRepository.findAllByThemeId(themeId);
    }

    @Override
    public Map<String, Object> listAsMapBy(String themeId) {
        // Convert to item map(key: item name, value: item)
        Map<String, Item> itemMap = getConfigItemMap(themeId);

        // Get theme setting
        List<ThemeSetting> themeSettings = listBy(themeId);

        Map<String, Object> result = new HashMap<>();

        // Build settings from user-defined
        themeSettings.forEach(themeSetting -> {
            String key = themeSetting.getKey();

            Item item = itemMap.get(key);

            if (item == null) {
                return;
            }

            Object convertedValue = item.getDataType().convertTo(themeSetting.getValue());
            log.debug("Converted user-defined data from [{}] to [{}], type: [{}]", themeSetting.getValue(), convertedValue, item.getDataType());

            result.put(key, convertedValue);
        });

        // Build settings from pre-defined
        itemMap.forEach((name, item) -> {
            log.debug("Name: [{}], item: [{}]", name, item);

            if (item.getDefaultValue() == null || result.containsKey(name)) {
                return;
            }

            // Set default value
            Object convertedDefaultValue = item.getDataType().convertTo(item.getDefaultValue());
            log.debug("Converted pre-defined data from [{}] to [{}], type: [{}]", item.getDefaultValue(), convertedDefaultValue, item.getDataType());

            result.put(name, convertedDefaultValue);
        });

        return result;
    }

    @Override
    public List<ThemeSetting> replaceUrl(String oldUrl, String newUrl) {
        List<ThemeSetting> themeSettings = listAll();
        List<ThemeSetting> replaced = new ArrayList<>();
        themeSettings.forEach(themeSetting -> {
            if (StringUtils.isNotEmpty(themeSetting.getValue())) {
                themeSetting.setValue(themeSetting.getValue().replaceAll(oldUrl, newUrl));
            }
            replaced.add(themeSetting);
        });
        return updateInBatch(replaced);
    }

    @Override
    @Transactional
    public void deleteInactivated() {
        themeSettingRepository.deleteByThemeIdIsNot(themeService.getActivatedThemeId());
    }

    /**
     * Gets config item map. (key: item name, value: item)
     *
     * @param themeId theme id must not be blank
     * @return config item map
     */
    private Map<String, Item> getConfigItemMap(@NonNull String themeId) {
        // Get theme configuration
        List<Group> groups = themeService.fetchConfig(themeId);

        // Mix all items
        Set<Item> items = new LinkedHashSet<>();
        groups.forEach(group -> items.addAll(group.getItems()));

        // Convert to item map(key: item name, value: item)
        return ServiceUtils.convertToMap(items, Item::getName);
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
