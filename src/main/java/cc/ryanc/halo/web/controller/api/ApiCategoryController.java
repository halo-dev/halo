package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <pre>
 *     文章分类API
 * </pre>
 *
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
     * <p>
     * result json:
     * <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": [
     *         {
     *             "cateId": "",
     *             "cateName": "",
     *             "cateUrl": "",
     *             "cateDesc": ""
     *         }
     *     ]
     * }
     *     </pre>
     * </p>
     *
     * @return JsonResult
     */
    @GetMapping
    public List<Category> categories() {
        return categoryService.listAll();
    }

    /**
     * 获取单个分类的信息
     *
     * <p>
     * result json:
     * <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": {
     *         "cateId": "",
     *         "cateName": "",
     *         "cateUrl": "",
     *         "cateDesc": ""
     *     }
     * }
     *     </pre>
     * </p>
     *
     * @param cateUrl 分类路径
     *
     * @return JsonResult
     */
    @GetMapping(value = "/{cateUrl}")
    public Category categories(@PathVariable("cateUrl") String cateUrl) {
        return categoryService.findByCateUrl(cateUrl);
    }
}
