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
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.*;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Blog archive page controller
 *
 * @author ryanwang
 * @date : 2019-03-17
 */
@Slf4j
@Controller
@RequestMapping(value = "/archives")
public class ContentArchiveController {

    private final PostService postService;

    private final PostCommentService postCommentService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final PostTagService postTagService;

    private final OptionService optionService;

    public ContentArchiveController(PostService postService,
                                    PostCommentService postCommentService,
                                    ThemeService themeService,
                                    PostCategoryService postCategoryService,
                                    PostTagService postTagService,
                                    OptionService optionService) {
        this.postService = postService;
        this.postCommentService = postCommentService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.postTagService = postTagService;
        this.optionService = optionService;
    }

    /**
     * Render post archives page.
     *
     * @param model model
     * @return template path : themes/{theme}/archives.ftl
     */
    @GetMapping
    public String archives(Model model) {
        return this.archives(model, 1, Sort.by(DESC, "createTime"));
    }

    /**
     * Render post archives page.
     *
     * @param model model
     * @return template path : themes/{theme}/archives.ftl
     */
    @GetMapping(value = "page/{page}")
    public String archives(Model model,
                           @PathVariable(value = "page") Integer page,
                           @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), sort);

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> postListVos = postService.convertToListVo(postPage);
        int[] pageRainbow = PageUtil.rainbow(page, postListVos.getTotalPages(), 3);

        model.addAttribute("is_archives", true);
        model.addAttribute("pageRainbow", pageRainbow);
        model.addAttribute("posts", postListVos);

        return themeService.render("archives");
    }

    /**
     * Render post page.
     *
     * @param url   post slug url.
     * @param cp    comment page number
     * @param model model
     * @return template path: themes/{theme}/post.ftl
     */
    @GetMapping("{url}")
    public String post(@PathVariable("url") String url,
                       @RequestParam(value = "cp", defaultValue = "1") Integer cp,
                       @SortDefault(sort = "createTime", direction = DESC) Sort sort,
                       Model model) {
        Post post = postService.getBy(PostStatus.PUBLISHED, url);

        postService.getNextPost(post.getCreateTime()).ifPresent(nextPost -> model.addAttribute("nextPost", nextPost));
        postService.getPrePost(post.getCreateTime()).ifPresent(prePost -> model.addAttribute("prePost", prePost));


        List<Category> categories = postCategoryService.listCategoryBy(post.getId());
        List<Tag> tags = postTagService.listTagsBy(post.getId());

        Page<BaseCommentVO> comments = postCommentService.pageVosBy(post.getId(), PageRequest.of(cp, optionService.getCommentPageSize(), sort));
        final int[] pageRainbow = PageUtil.rainbow(cp, comments.getTotalPages(), 3);

        model.addAttribute("is_post", true);
        model.addAttribute("post", post);
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("comments", comments);
        model.addAttribute("pageRainbow", pageRainbow);

        return themeService.render("post");
    }
}
