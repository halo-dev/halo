package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Menu;
import cc.ryanc.halo.repository.MenuRepository;
import cc.ryanc.halo.service.MenuService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * MenuService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class MenuServiceImpl extends AbstractCrudService<Menu, Integer> implements MenuService {

    private final MenuRepository menuRepository;

    public MenuServiceImpl(MenuRepository menuRepository) {
        super(menuRepository);
        this.menuRepository = menuRepository;
    }
}
