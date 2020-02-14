package run.halo.app.controller.content.model;

import cn.hutool.core.util.PageUtil;
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

    public String list(Model model) {
        model.addAttribute("is_categories", true);
        return themeService.render("categories");
    }

    public String listPost(Model model, String slugName, Integer page) {
        // Get category by slug name
        final Category category = categoryService.getBySlugNameOfNonNull(slugName);
        CategoryDTO categoryDTO = categoryService.convertTo(category);

        final Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), Sort.by(DESC, "createTime"));
        Page<Post> postPage = postCategoryService.pagePostBy(category.getId(), PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);

        // TODO remove this variable
        final int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);

        // Next page and previous page url.
        StringBuilder nextPageFullPath = new StringBuilder(categoryDTO.getFullPath());
        StringBuilder prePageFullPath = new StringBuilder(categoryDTO.getFullPath());

        nextPageFullPath.append("/page/")
                .append(posts.getNumber() + 2)
                .append(optionService.getPathSuffix());

        if (posts.getNumber() == 1) {
            prePageFullPath.append("/");
        } else {
            prePageFullPath.append("/page/")
                    .append(posts.getNumber())
                    .append(optionService.getPathSuffix());
        }

        model.addAttribute("is_category", true);
        model.addAttribute("posts", posts);
        model.addAttribute("rainbow", rainbow);
        model.addAttribute("category", categoryDTO);
        model.addAttribute("nextPageFullPath", nextPageFullPath.toString());
        model.addAttribute("prePageFullPath", prePageFullPath.toString());
        return themeService.render("category");
    }
}
