package run.halo.app.controller.content.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.MenuDTO;
import run.halo.app.model.vo.MenuVO;
import run.halo.app.service.MenuService;

/**
 * Content menu controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-03
 */
@RestController("ApiContentMenuController")
@RequestMapping("/api/content/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    @ApiOperation("Lists all menus")
    public List<MenuDTO> listAll(@SortDefault(sort = "priority", direction = DESC) Sort sort) {
        return menuService.listDtos(sort);
    }

    @GetMapping(value = "tree_view")
    @ApiOperation("Lists menus with tree view")
    public List<MenuVO> listMenusTree(
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return menuService.listAsTree(sort);
    }
}
