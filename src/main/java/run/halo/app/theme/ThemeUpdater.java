package run.halo.app.theme;

import java.io.IOException;
import run.halo.app.handler.theme.config.support.ThemeProperty;

/**
 * Theme updater.
 *
 * @author johnniang
 */
public interface ThemeUpdater {

    /**
     * Update theme property.
     *
     * @param themeId theme id
     * @return updated theme property
     */
    ThemeProperty update(String themeId) throws IOException;

}
