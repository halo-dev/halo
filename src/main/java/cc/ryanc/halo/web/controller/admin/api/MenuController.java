package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.MenuOutputDTO;
import cc.ryanc.halo.model.entity.Menu;
import cc.ryanc.halo.model.params.MenuParam;
import cc.ryanc.halo.service.MenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

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
}
