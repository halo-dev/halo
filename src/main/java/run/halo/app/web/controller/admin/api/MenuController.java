package run.halo.app.web.controller.admin.api;

import run.halo.app.model.dto.MenuOutputDTO;
import run.halo.app.model.entity.Menu;
import run.halo.app.model.params.MenuParam;
import run.halo.app.service.MenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.entity.Menu;
import run.halo.app.service.MenuService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

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

    @GetMapping
    @ApiOperation("Lists all menus")
    public List<MenuOutputDTO> listAll(@SortDefault(sort = "sort", direction = DESC) Sort sort) {
        return menuService.listDtos(sort);
    }

    @PostMapping
    @ApiOperation("Creates a menu")
    public MenuOutputDTO createBy(@RequestBody @Valid MenuParam menuParam) {
        return new MenuOutputDTO().convertFrom(menuService.createBy(menuParam));
    }

    @PutMapping("{menuId:\\d+}")
    @ApiOperation("Updates a menu")
    public MenuOutputDTO updateBy(@PathVariable("menuId") Integer menuId,
                                  @RequestBody @Valid MenuParam menuParam) {
        // Get the menu
        Menu menu = menuService.getById(menuId);

        // Update changed properties of the menu
        menuParam.update(menu);

        // Update menu in database
        return new MenuOutputDTO().convertFrom(menuService.update(menu));
    }

    @DeleteMapping("{menuId:\\d+}")
    @ApiOperation("Deletes a menu")
    public MenuOutputDTO deleteBy(@PathVariable("menuId") Integer menuId) {
        return new MenuOutputDTO().convertFrom(menuService.removeById(menuId));
    }
}
