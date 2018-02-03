package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Menu;
import cc.ryanc.halo.repository.MenuRepository;
import cc.ryanc.halo.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/24
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
}
