package run.halo.app.theme;

import run.halo.app.handler.theme.config.support.ThemeProperty;

/**
 * Theme updater.
 *
 * @author johnniang
 */
public interface ThemeUpdater {
    /**
     * Check whether current source is supported or not
     *
     * @param source new theme source
     * @return true if supported, false otherwise
     */
    boolean support(Object source);

    /**
     * Update theme property.
     *
     * @param themeId theme id
     * @param source  new theme source
     * @return updated theme property
     */
    ThemeProperty update(String themeId, Object source);

}
