package run.halo.app.theme.finders;

import run.halo.app.theme.finders.vo.ThemeVo;

/**
 * A finder for theme.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface ThemeFinder {

    ThemeVo activation();

    ThemeVo getByName(String themeName);
}
