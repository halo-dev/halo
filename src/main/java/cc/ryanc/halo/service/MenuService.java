package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Menu;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/24
 */
public interface MenuService {

    /**
     * 查询所有菜单
     *
     * @return list
     */
    List<Menu> findAllMenus();
}
