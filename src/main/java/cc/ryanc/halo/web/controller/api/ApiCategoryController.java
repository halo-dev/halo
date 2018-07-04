package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResponseStatus;
import cc.ryanc.halo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@RestController
@RequestMapping(value = "/api/categories")
public class ApiCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类
     *
     * @return JsonResult
     */
    @GetMapping
    public JsonResult categories(){
        List<Category> categories = categoryService.findAllCategories();
        if(null!=categories && categories.size()>0){
            return new JsonResult(ResponseStatus.SUCCESS.getCode(),ResponseStatus.SUCCESS.getMsg(),categories);
        }else{
            return new JsonResult(ResponseStatus.EMPTY.getCode(),ResponseStatus.EMPTY.getMsg());
        }
    }

    /**
     * 获取单个分类的信息
     *
     * @param cateUrl 分类路径
     * @return JsonResult
     */
    @GetMapping(value = "/{cateUrl}")
    public JsonResult categories(@PathVariable("cateUrl") String cateUrl){
        Category category = categoryService.findByCateUrl(cateUrl);
        if(null!=category){
            return new JsonResult(ResponseStatus.SUCCESS.getCode(),ResponseStatus.SUCCESS.getMsg(),category);
        }else{
            return new JsonResult(ResponseStatus.EMPTY.getCode(),ResponseStatus.EMPTY.getMsg());
        }
    }
}
