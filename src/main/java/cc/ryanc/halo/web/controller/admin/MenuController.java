package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Menu;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2018/1/30
 * @version : 1.0
 * description :
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
     * @return string
     */
    @GetMapping
    public String menus(Model model){
        List<Menu> menus = menuService.findAllMenus();
        model.addAttribute("menus",menus);
        model.addAttribute("statusName","添加");
        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "/admin/admin_menu";
    }

    /**
     * 新增/修改菜单
     *
     * @param menu menu
     * @return string
     */
    @PostMapping(value = "/save")
    public String saveMenu(@ModelAttribute Menu menu){
        try{
            menuService.saveByMenu(menu);
        }catch (Exception e){
            log.error("保存菜单失败："+e.getMessage());
        }
        return "redirect:/admin/menus";
    }

    /**
     * 跳转到修改页面
     * @param id id
     * @param model model
     * @return string
     */
    @GetMapping(value = "/edit")
    public String updateMenu(@RequestParam("menuId") Long menuId,Model model){
        List<Menu> menus = menuService.findAllMenus();
        Menu menu = menuService.findByMenuId(menuId).get();
        model.addAttribute("statusName","修改");
        model.addAttribute("updateMenu",menu);
        model.addAttribute("menus",menus);
        //设置选项
        model.addAttribute("options", HaloConst.OPTIONS);
        return "/admin/admin_menu";
    }
}
