package run.halo.app.web.controller.content;

import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import run.halo.app.model.entity.Category;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostService;
import run.halo.app.service.ThemeService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author : RYAN0UP
 * @date : 2019/3/20
 */
@Controller
@RequestMapping(value = "/categories")
public class ContentCategoryController {

    private final CategoryService categoryService;

    private final PostService postService;

    private final ThemeService themeService;

    public ContentCategoryController(CategoryService categoryService,
                                     PostService postService,
                                     ThemeService themeService) {
        this.categoryService = categoryService;
        this.postService = postService;
        this.themeService = themeService;
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
        return themeService.render("categories");
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
        // TODO Complete this api in the future
        return "";
    }
}
