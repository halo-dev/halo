package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.service.MenuService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Menu controller.
 *
 * @author johnniang
 * @date 4/3/19
 */
@RestController
@RequestMapping("/admin/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

}
