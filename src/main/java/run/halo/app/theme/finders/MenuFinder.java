package run.halo.app.theme.finders;

import run.halo.app.theme.finders.vo.MenuVo;

/**
 * A finder for {@link run.halo.app.core.extension.Menu}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface MenuFinder {

    MenuVo getByName(String name);

    MenuVo getDefault();
}
