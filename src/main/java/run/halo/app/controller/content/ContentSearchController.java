package run.halo.app.controller.content;

import cn.hutool.core.util.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;
import run.halo.app.model.entity.Post;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.ThemeService;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Search controller.
 *
 * @author ryanwang
 * @date 2019-04-21
 */
@Controller
@RequestMapping(value = "/search")
public class ContentSearchController {

    private final PostService postService;

    private final OptionService optionService;

    private final ThemeService themeService;

    public ContentSearchController(PostService postService, OptionService optionService, ThemeService themeService) {
        this.postService = postService;
        this.optionService = optionService;
        this.themeService = themeService;
    }

    /**
     * Render post search page.
     *
     * @param model   model
     * @param keyword keyword
     * @return template path : themes/{theme}/search.ftl
     */
    @GetMapping
    public String search(Model model,
                         @RequestParam(value = "keyword") String keyword) {
        return this.search(model, HtmlUtils.htmlEscape(keyword), 1, Sort.by(DESC, "createTime"));
    }

    /**
     * Render post search page.
     *
     * @param model   model
     * @param keyword keyword
     * @return template path :themes/{theme}/search.ftl
     */
    @GetMapping(value = "page/{page}")
    public String search(Model model,
                         @RequestParam(value = "keyword") String keyword,
                         @PathVariable(value = "page") Integer page,
                         @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        final Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), sort);
        final Page<Post> postPage = postService.pageBy(keyword, pageable);

        final Page<PostListVO> posts = postService.convertToListVo(postPage);

        // TODO remove this variable
        final int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);

        // Next page and previous page url.
        StringBuilder nextPageFullPath = new StringBuilder();
        StringBuilder prePageFullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()) {
            nextPageFullPath.append(optionService.getBlogBaseUrl())
                    .append("/");
            prePageFullPath.append(optionService.getBlogBaseUrl())
                    .append("/");
        } else {
            nextPageFullPath.append("/");
            prePageFullPath.append("/");
        }

        nextPageFullPath.append("search");
        prePageFullPath.append("search");

        nextPageFullPath.append("/page/")
                .append(posts.getNumber() + 2)
                .append(optionService.getPathSuffix())
                .append("?keyword=")
                .append(keyword);

        if (posts.getNumber() == 1) {
            prePageFullPath.append("?keyword=")
                    .append(keyword);
        } else {
            prePageFullPath.append("/page/")
                    .append(posts.getNumber())
                    .append(optionService.getPathSuffix())
                    .append("?keyword=")
                    .append(keyword);
        }

        model.addAttribute("is_search", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("posts", posts);
        model.addAttribute("rainbow", rainbow);
        model.addAttribute("nextPageFullPath", nextPageFullPath.toString());
        model.addAttribute("prePageFullPath", prePageFullPath.toString());
        return themeService.render("search");
    }
}
