package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResponseStatusEnum;
import cc.ryanc.halo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <pre>
 *     文章分类API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/categories")
public class ApiCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类
     *
     * <p>
     *     result json:
     *     <pre>
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
    public JsonResult categories() {
        List<Category> categories = categoryService.findAll();
        if (null != categories && categories.size() > 0) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), categories);
        } else {
            return new JsonResult(ResponseStatusEnum.EMPTY.getCode(), ResponseStatusEnum.EMPTY.getMsg());
        }
    }

    /**
     * 获取单个分类的信息
     *
     * <p>
     *     result json:
     *     <pre>
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
     * @return JsonResult
     */
    @GetMapping(value = "/{cateUrl}")
    public JsonResult categories(@PathVariable("cateUrl") String cateUrl) {
        final Category category = categoryService.findByCateUrl(cateUrl);
        if (null != category) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), category);
        } else {
            return new JsonResult(ResponseStatusEnum.EMPTY.getCode(), ResponseStatusEnum.EMPTY.getMsg());
        }
    }
}
