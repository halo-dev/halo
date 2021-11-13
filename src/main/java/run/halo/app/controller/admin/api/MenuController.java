package run.halo.app.controller.admin.api;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.MenuDTO;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Menu;
import run.halo.app.model.params.MenuParam;
import run.halo.app.model.vo.MenuVO;
import run.halo.app.service.MenuService;

/**
 * Menu controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-03
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
    public List<MenuDTO> listAll(@SortDefault(sort = "team", direction = DESC) Sort sort) {
        return menuService.listDtos(sort.and(Sort.by(ASC, "priority")));
    }

    @GetMapping("tree_view")
    @ApiOperation("Lists menus as tree")
    public List<MenuVO> listAsTree(@SortDefault(sort = "team", direction = DESC) Sort sort) {
        return menuService.listAsTree(sort.and(Sort.by(ASC, "priority")));
    }

    @GetMapping("team/tree_view")
    @ApiOperation("Lists menus as tree by team")
    public List<MenuVO> listDefaultsAsTreeByTeam(
        @SortDefault(sort = "priority", direction = ASC) Sort sort,
        @RequestParam(name = "team") String team) {
        return menuService.listByTeamAsTree(team, sort);
    }

    @GetMapping("{menuId:\\d+}")
    @ApiOperation("Gets menu detail by id")
    public MenuDTO getBy(@PathVariable("menuId") Integer menuId) {
        return new MenuDTO().convertFrom(menuService.getById(menuId));
    }

    @PostMapping
    @ApiOperation("Creates a menu")
    public MenuDTO createBy(@RequestBody @Valid MenuParam menuParam) {
        return new MenuDTO().convertFrom(menuService.createBy(menuParam));
    }

    @PostMapping("/batch")
    public List<MenuDTO> createBatchBy(@RequestBody @Valid List<MenuParam> menuParams) {
        List<Menu> menus = menuParams
            .stream()
            .map(InputConverter::convertTo)
            .collect(Collectors.toList());
        return menuService.createInBatch(menus).stream()
            .map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu))
            .collect(Collectors.toList());
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

    @PutMapping("/batch")
    public List<MenuDTO> updateBatchBy(@RequestBody @Valid List<MenuParam> menuParams) {
        List<Menu> menus = menuParams
            .stream()
            .filter(menuParam -> Objects.nonNull(menuParam.getId()))
            .map(InputConverter::convertTo)
            .collect(Collectors.toList());
        return menuService.updateInBatch(menus).stream()
            .map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu))
            .collect(Collectors.toList());
    }

    @DeleteMapping("{menuId:\\d+}")
    @ApiOperation("Deletes a menu")
    public MenuDTO deleteBy(@PathVariable("menuId") Integer menuId) {
        List<Menu> menus = menuService.listByParentId(menuId);
        if (null != menus && menus.size() > 0) {
            menus.forEach(menu -> {
                menu.setParentId(0);
                menuService.update(menu);
            });
        }
        return new MenuDTO().convertFrom(menuService.removeById(menuId));
    }

    @DeleteMapping("/batch")
    public List<MenuDTO> deleteBatchBy(@RequestBody List<Integer> menuIds) {
        List<Menu> menus = menuService.listAllByIds(menuIds);
        menuService.removeInBatch(menuIds);
        return menus.stream()
            .map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu))
            .collect(Collectors.toList());
    }

    @GetMapping("teams")
    @ApiOperation("Lists all menu teams")
    public List<String> teams() {
        return menuService.listAllTeams();
    }
}
