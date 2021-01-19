package run.halo.app.repository;

import java.util.List;
import java.util.Optional;
import run.halo.app.handler.theme.config.support.ThemeProperty;

/**
 * Theme repository.
 *
 * @author johnniang
 */
public interface ThemeRepository {

    /**
     * Get activated theme id.
     *
     * @return activated theme id
     */
    String getActivatedThemeId();

    /**
     * Get activated theme property.
     *
     * @return activated theme property
     */
    ThemeProperty getActivatedThemeProperty();

    /**
     * Fetch theme property by theme id.
     *
     * @param themeId theme id
     * @return an optional theme property
     */
    Optional<ThemeProperty> fetchThemePropertyByThemeId(String themeId);

    /**
     * List all themes
     *
     * @return theme list
     */
    List<ThemeProperty> listAll();

    /**
     * Set activated theme.
     *
     * @param themeId theme id
     */
    void setActivatedTheme(String themeId);

}