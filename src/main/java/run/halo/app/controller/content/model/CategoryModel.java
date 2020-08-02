package run.halo.app.controller.content.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Category Model.
 *
 * @author ryanwang
 * @date 2020-01-11
 */
@Component
public class CategoryModel {

    private final CategoryService categoryService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final PostService postService;

    private final OptionService optionService;

    public CategoryModel(CategoryService categoryService, ThemeService themeService, PostCategoryService postCategoryService, PostService postService, OptionService optionService) {
        this.categoryService = categoryService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.postService = postService;
        this.optionService = optionService;
    }

    /**
     * List categories.
     *
     * @param model model
     * @return template name
     */
    public String list(Model model) {
        model.addAttribute("is_categories", true);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("categories");
    }

    /**
     * List category posts.
     *
     * @param model model
     * @param slug  slug
     * @param page  current page
     * @return template name
     */
    public String listPost(Model model, String slug, Integer page) {
        // Get category by slug
        final Category category = categoryService.getBySlugOfNonNull(slug);
        CategoryDTO categoryDTO = categoryService.convertTo(category);

        final Pageable pageable = PageRequest.of(page - 1,
                optionService.getArchivesPageSize(),
                Sort.by(DESC, "topPriority", "createTime"));
        Page<Post> postPage = postCategoryService.pagePostBy(category.getId(), PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);

        // Generate meta description.
        if (StringUtils.isNotEmpty(category.getDescription())) {
            model.addAttribute("meta_description", category.getDescription());
        } else {
            model.addAttribute("meta_description", optionService.getSeoDescription());
        }

        model.addAttribute("is_category", true);
        model.addAttribute("posts", posts);
        model.addAttribute("category", categoryDTO);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        return themeService.render("category");
    }
}
