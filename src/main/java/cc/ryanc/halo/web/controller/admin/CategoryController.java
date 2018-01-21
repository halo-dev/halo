package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.RespStatus;
import cc.ryanc.halo.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2017/12/10
 * @version : 1.0
 * description : 分类目录控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类并渲染category页面
     * @param model model
     * @return freemarker页面
     */
    @GetMapping
    public String categories(Model model){
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);
        model.addAttribute("options", HaloConst.OPTIONS);
        return "admin/category";
    }

    /**
     * 新增分类目录
     * @param category category对象
     * @return freemarker页面
     */
    @PostMapping(value = "/save")
    public String saveCategory(@ModelAttribute Category category){
        try{
            Category backCate = categoryService.saveByCategory(category);
            log.info("新添加的分类目录为："+backCate);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/category";
    }

    /**
     * 验证分类目录路径是否已经存在
     * @param cateUrl cateUrl
     * @return string
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public String checkCateUrlExists(@RequestParam("cateUrl") String cateUrl){
        Category category = categoryService.findByCateUrl(cateUrl);
        if(null!=category){
            return RespStatus.EXISTS;
        }else{
            return RespStatus.NOTEXISTS;
        }
    }

    /**
     * 处理删除分类目录的请求
     * @param cateId cateId
     * @return freemarker
     */
    @GetMapping(value = "/remove")
    public String removeCategory(@PathParam("cateId") Integer cateId){
        try{
            Category category = categoryService.removeByCateId(cateId);
            log.info("删除的分类目录："+category);
        } catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/category";
    }

    /**
     * 处理修改分类的请求
     * @param category category
     * @return redirect
     */
    @PostMapping(value = "/update")
    public String updateCategory(@ModelAttribute Category category){
        try{
            Category beforeCate = categoryService.findByCateId(category.getCateId());
            log.info("修改之前的数据："+beforeCate+"，修改之后的数据："+category);
            categoryService.updateByCategory(category);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/category";
    }

    /**
     * 跳转到修改页面
     * @param cateId cateId
     * @param model model
     * @return String
     */
    @GetMapping(value = "/edit")
    public String toEditCategory(Model model,@PathParam("cateId") Integer cateId){
        try{
            Category category = categoryService.findByCateId(cateId);
            model.addAttribute("category",category);
            model.addAttribute("options", HaloConst.OPTIONS);
            log.info("cateId为"+cateId+"的数据为："+category);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "admin/_cate-update";
    }
}
