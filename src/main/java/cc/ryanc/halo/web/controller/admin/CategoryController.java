package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Categories controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * categories manage
     *
     * @return template path: admin/admin_category.ftl
     */
    @GetMapping
    public String categories() {
        return "admin/admin_category";
    }
}
