package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Menu;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2018/1/24
 * @version : 1.0
 * description :
 */
public interface MenuService {

    /**
     * 新增菜单
     *
     * @param menu menu
     * @return Menu
     */
    Menu saveByMenu(Menu menu);

    /**
     * 查询所有菜单
     *
     * @return list
     */
    List<Menu> findAllMenus();

    /**
     * 删除菜单
     *
     * @param menuId menuId
     * @return menu
     */
    Menu removeByMenuId(Long menuId);

    /**
     * 修改菜单
     * @param menu menu
     * @return Menu
     */
    Menu updateByMenu(Menu menu);

    /**
     * 根据编号查询菜单
     * @param menuId menuId
     * @return Menu
     */
    Optional<Menu> findByMenuId(Long menuId);
}
