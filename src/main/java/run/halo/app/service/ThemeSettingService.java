package run.halo.app.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.entity.ThemeSetting;
import run.halo.app.service.base.CrudService;

import java.util.List;
import java.util.Map;

/**
 * Theme setting service interface.
 *
 * @author johnniang
 * @date 2019-04-08
 */
public interface ThemeSettingService extends CrudService<ThemeSetting, Integer> {


    /**
     * Saves theme setting.
     *
     * @param key     setting key must not be blank
     * @param value   setting value
     * @param themeId theme id must not be blank
     * @return theme setting or null if the key does not exist
     */
    @Nullable
    @Transactional
    ThemeSetting save(@NonNull String key, @Nullable String value, @NonNull String themeId);

    /**
     * Saves theme settings.
     *
     * @param settings theme setting map
     * @param themeId  theme id must not be blank
     */
    @Transactional
    void save(@Nullable Map<String, Object> settings, @NonNull String themeId);

    /**
     * Lists theme settings by theme id.
     *
     * @param themeId theme id must not be blank
     * @return a list of theme setting
     */
    @NonNull
    List<ThemeSetting> listBy(String themeId);

    /**
     * Lists theme settings as map.
     *
     * @param themeId theme id must not be blank
     * @return theme setting map
     */
    @NonNull
    Map<String, Object> listAsMapBy(@NonNull String themeId);

    /**
     * Replace theme setting url in batch.
     *
     * @param oldUrl old blog url.
     * @param newUrl new blog url.
     * @return replaced theme settings.
     */
    List<ThemeSetting> replaceUrl(@NonNull String oldUrl, @NonNull String newUrl);

    /**
     * Delete unused theme setting.
     */
    void deleteInactivated();
}
