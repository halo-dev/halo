package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Menu;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/30
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/menu")
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
    public String menu(Model model){
        List<Menu> menus = menuService.findAllMenus();
        model.addAttribute("menus",menus);
        return "/admin/admin_menu";
    }

    /**
     * 新增菜单
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
        return "redirect:/admin/menu";
    }
}
