package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Menu;
import cc.ryanc.halo.repository.MenuRepository;
import cc.ryanc.halo.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2018/1/24
 * @version : 1.0
 * description :
 */
@Service
public class MenuServiceImpl implements MenuService{

    @Autowired
    private MenuRepository menuRepository;

    /**
     * 查询所有菜单
     *
     * @return list
     */
    @Override
    public List<Menu> findAllMenus() {
        return menuRepository.findAll();
    }

    /**
     * 新增菜单
     *
     * @param menu menu
     * @return Menu
     */
    @Override
    public Menu saveByMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    /**
     * 删除菜单
     *
     * @param menuId menuId
     * @return menu
     */
    @Override
    public Menu removeByMenuId(Long menuId) {
        Optional<Menu> menu = this.findByMenuId(menuId);
        menuRepository.delete(menu.get());
        return menu.get();
    }

    /**
     * 修改菜单
     *
     * @param menu menu
     * @return Menu
     */
    @Override
    public Menu updateByMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    /**
     * 根据编号查询菜单
     *
     * @param menuId menuId
     * @return Menu
     */
    @Override
    public Optional<Menu> findByMenuId(Long menuId) {
        return menuRepository.findById(menuId);
    }
}
