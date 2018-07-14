package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类并渲染category页面
     *
     * @return 模板路径admin/admin_category
     */
    @GetMapping
    public String categories() {
        return "admin/admin_category";
    }

    /**
     * 新增/修改分类目录
     *
     * @param category category对象
     * @return 重定向到/admin/category
     */
    @PostMapping(value = "/save")
    public String saveCategory(@ModelAttribute Category category) {
        try {
            categoryService.saveByCategory(category);
        } catch (Exception e) {
            log.error("未知错误：{0}", e.getMessage());
        }
        return "redirect:/admin/category";
    }

    /**
     * 验证分类目录路径是否已经存在
     *
     * @param cateUrl 分类路径
     * @return true：不存在，false：存在
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public boolean checkCateUrlExists(@RequestParam("cateUrl") String cateUrl) {
        Category category = categoryService.findByCateUrl(cateUrl);
        return null != category;
    }

    /**
     * 处理删除分类目录的请求
     *
     * @param cateId cateId
     * @return 重定向到/admin/category
     */
    @GetMapping(value = "/remove")
    public String removeCategory(@PathParam("cateId") Long cateId) {
        try {
            Category category = categoryService.removeByCateId(cateId);
            log.info("删除的分类目录：" + category);
        } catch (Exception e) {
            log.error("未知错误：{}", e.getMessage());
        }
        return "redirect:/admin/category";
    }

    /**
     * 跳转到修改页面
     *
     * @param cateId cateId
     * @param model  model
     * @return 模板路径admin/admin_category
     */
    @GetMapping(value = "/edit")
    public String toEditCategory(Model model, @PathParam("cateId") Long cateId) {
        Optional<Category> category = categoryService.findByCateId(cateId);
        model.addAttribute("updateCategory", category.get());
        return "admin/admin_category";
    }
}
