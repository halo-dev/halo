package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Menu;
import cc.ryanc.halo.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * <pre>
 *     后台菜单管理控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/30
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 渲染菜单设置页面
     *
     * @param model model
     * @return 模板路径/admin/admin_menu
     */
    @GetMapping
    public String menus() {
        return "/admin/admin_menu";
    }

    /**
     * 新增/修改菜单
     *
     * @param menu menu
     * @return 重定向到/admin/menus
     */
    @PostMapping(value = "/save")
    public String saveMenu(@ModelAttribute Menu menu) {
        try {
            menuService.saveByMenu(menu);
        } catch (Exception e) {
            log.error("保存菜单失败：{}" + e.getMessage());
        }
        return "redirect:/admin/menus";
    }

    /**
     * 跳转到修改页面
     *
     * @param menuId 菜单编号
     * @param model  model
     * @return 模板路径/admin/admin_menu
     */
    @GetMapping(value = "/edit")
    public String updateMenu(@RequestParam("menuId") Long menuId, Model model) {
        Menu menu = menuService.findByMenuId(menuId).get();
        model.addAttribute("updateMenu", menu);
        return "/admin/admin_menu";
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单编号
     * @return 重定向到/admin/menus
     */
    @GetMapping(value = "/remove")
    public String removeMenu(@RequestParam("menuId") Long menuId) {
        try {
            menuService.removeByMenuId(menuId);
        } catch (Exception e) {
            log.error("删除菜单失败：{}", e.getMessage());
        }
        return "redirect:/admin/menus";
    }
}
