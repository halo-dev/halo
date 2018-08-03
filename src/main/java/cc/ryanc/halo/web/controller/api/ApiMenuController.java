package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Menu;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResponseStatusEnum;
import cc.ryanc.halo.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <pre>
 *     菜单API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@RestController
@RequestMapping(value = "/api/menus")
public class ApiMenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取所有菜单
     *
     * @return JsonResult
     */
    @GetMapping
    public JsonResult menus() {
        List<Menu> menus = menuService.findAllMenus();
        if (null != menus && menus.size() > 0) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), menus);
        } else {
            return new JsonResult(ResponseStatusEnum.EMPTY.getCode(), ResponseStatusEnum.EMPTY.getMsg());
        }
    }
}
