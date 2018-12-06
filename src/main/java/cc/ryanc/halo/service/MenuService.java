package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Menu;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     菜单业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/24
 */
public interface MenuService {

    /**
     * 新增/修改菜单
     *
     * @param menu menu
     * @return Menu
     */
    Menu save(Menu menu);

    /**
     * 查询所有菜单
     *
     * @return List
     */
    List<Menu> findAll();

    /**
     * 删除菜单
     *
     * @param menuId menuId
     * @return Menu
     */
    Menu remove(Long menuId);

    /**
     * 根据编号查询菜单
     *
     * @param menuId menuId
     * @return Optional
     */
    Optional<Menu> findByMenuId(Long menuId);
}
