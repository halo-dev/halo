package run.halo.app.theme;

import org.springframework.web.multipart.MultipartFile;
import run.halo.app.handler.theme.config.support.ThemeProperty;

/**
 * Multipart file theme updater.
 *
 * @author johnniang
 */
public class MultipartFileThemeUpdater implements ThemeUpdater {

    @Override
    public boolean support(Object source) {
        return source instanceof MultipartFile;
    }

    @Override
    public ThemeProperty update(String themeId, Object source) {
        return null;
    }
}
