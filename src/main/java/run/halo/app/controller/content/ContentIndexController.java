package run.halo.app.controller.content;

import cn.hutool.core.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
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
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.ThemeService;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Blog index page controller
 *
 * @author ryanwang
 * @date : 2019-03-17
 */
@Slf4j
@Controller
@RequestMapping
public class ContentIndexController {

    private final PostService postService;

    private final OptionService optionService;

    private final ThemeService themeService;

    public ContentIndexController(PostService postService,
                                  OptionService optionService,
                                  ThemeService themeService) {
        this.postService = postService;
        this.optionService = optionService;
        this.themeService = themeService;
    }


    /**
     * Render blog index
     *
     * @param model model
     * @return template path: themes/{theme}/index.ftl
     */
    @GetMapping
    public String index(Model model) {
        return this.index(model, 1, Sort.by(DESC, "topPriority").and(Sort.by(DESC, "createTime")));
    }

    /**
     * Render blog index
     *
     * @param model model
     * @param page  current page number
     * @return template path: themes/{theme}/index.ftl
     */
    @GetMapping(value = "page/{page}")
    public String index(Model model,
                        @PathVariable(value = "page") Integer page,
                        @SortDefault.SortDefaults({
                                @SortDefault(sort = "topPriority", direction = DESC),
                                @SortDefault(sort = "createTime", direction = DESC)
                        }) Sort sort) {
        log.debug("Requested index page, sort info: [{}]", sort);
        int pageSize = optionService.getPostPageSize();
        Pageable pageable = PageRequest.of(page >= 1 ? page - 1 : page, pageSize, sort);

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);

        int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);

        model.addAttribute("is_index", true);
        model.addAttribute("posts", posts);
        model.addAttribute("rainbow", rainbow);
        return themeService.render("index");
    }
}
