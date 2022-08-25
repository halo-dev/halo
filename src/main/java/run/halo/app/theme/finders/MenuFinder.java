package run.halo.app.theme.finders;

import java.util.List;
import run.halo.app.theme.finders.vo.MenuItemVo;
import run.halo.app.theme.finders.vo.MenuVo;

/**
 * A finder for {@link run.halo.app.core.extension.Menu}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface MenuFinder {

    List<MenuVo> listAll();

    List<MenuVo> listAsTree();

    List<MenuItemVo> listAllMenuItem();
}
