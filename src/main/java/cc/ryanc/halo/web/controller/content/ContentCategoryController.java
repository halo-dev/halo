package cc.ryanc.halo.web.controller.content;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author : RYAN0UP
 * @date : 2019/3/20
 */
@Controller
@RequestMapping(value = "/categories")
public class ContentCategoryController extends BaseContentController {

    private final CategoryService categoryService;

    private final PostService postService;

    public ContentCategoryController(CategoryService categoryService,
                                     PostService postService) {
        this.categoryService = categoryService;
        this.postService = postService;
    }

    /**
     * Render category list page
     *
     * @param model model
     * @return template path: /{theme}/categories.ftl
     */
    @GetMapping
    public String categories(Model model) {
        final List<Category> categories = categoryService.listAll();
        model.addAttribute("categories", categories);
        return this.render("categories");
    }

    /**
     * Render post list page by category
     *
     * @param model    model
     * @param slugName slugName
     * @return template path: /{theme}/category.ftl
     */
    @GetMapping(value = "{slugName}")
    public String categories(Model model,
                             @PathVariable("slugName") String slugName) {
        return this.categories(model, slugName, 1, Sort.by(DESC, "postDate"));
    }

    /**
     * Render post list page by category
     *
     * @param model    model
     * @param slugName slugName
     * @param page     current page number
     * @return template path: /{theme}/category.ftl
     */
    @GetMapping("{slugName}/page/{page}")
    public String categories(Model model,
                             @PathVariable("slugName") String slugName,
                             @PathVariable("page") Integer page,
                             @SortDefault(sort = "postDate", direction = DESC) Sort sort) {
        return "";
    }
}
