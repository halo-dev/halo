package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.MenuDTO;
import run.halo.app.model.entity.Menu;
import run.halo.app.model.params.MenuParam;
import run.halo.app.model.vo.MenuVO;
import run.halo.app.service.MenuService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Menu controller.
 *
 * @author johnniang
 * @date 4/3/19
 */
@RestController
@RequestMapping("/api/admin/menus")
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

    @GetMapping("tree_view")
    @ApiOperation("List as category tree")
    public List<MenuVO> listAsTree(@SortDefault(sort = "name", direction = ASC) Sort sort) {
        return menuService.listAsTree(sort);
    }

    /**
     * Get menu by menuId.
     *
     * @param menuId menuId
     * @return MenuDTO
     */
    @GetMapping("{menuId:\\d+}")
    @ApiOperation("Get menu detail by id")
    public MenuDTO getBy(@PathVariable("menuId") Integer menuId) {
        return new MenuDTO().convertFrom(menuService.getById(menuId));
    }

    @PostMapping
    @ApiOperation("Creates a menu")
    public MenuDTO createBy(@RequestBody @Valid MenuParam menuParam) {
        return new MenuDTO().convertFrom(menuService.createBy(menuParam));
    }

    @PutMapping("{menuId:\\d+}")
    @ApiOperation("Updates a menu")
    public MenuDTO updateBy(@PathVariable("menuId") Integer menuId,
                            @RequestBody @Valid MenuParam menuParam) {
        // Get the menu
        Menu menu = menuService.getById(menuId);

        // Update changed properties of the menu
        menuParam.update(menu);

        // Update menu in database
        return new MenuDTO().convertFrom(menuService.update(menu));
    }

    @DeleteMapping("{menuId:\\d+}")
    @ApiOperation("Deletes a menu")
    public MenuDTO deleteBy(@PathVariable("menuId") Integer menuId) {
        return new MenuDTO().convertFrom(menuService.removeById(menuId));
    }
}
